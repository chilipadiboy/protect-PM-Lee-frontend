import React, { Component } from 'react';
import './MFA.css';
import { Button } from 'antd';
import {sign, hash} from 'tweetnacl';

const connectMessage = "Please connect your bluetooth tag for multi-factor authentication!";
const successfulConnectMessage = "Connection was successful! Web has been verified!";
const connectButton = "Connect";
const absentButton = "No Tag";
const keyPair = sign.keyPair();
let writeChar, readChar, disconnectChar;
let verified = false;
//this should be retrieved from the server as
// {publicKey: Uint8Array(32), secretKey: Uint8Array(32)}

class MFA extends Component {
  constructor(props) {
    super(props);
    this.state = {
      verifiedState: verified,
    }
  }

  startConnection() {
   let context = this;
    navigator.bluetooth.requestDevice({
      filters: [ {services:[0x2220]} ]
    })
      .then(device => {
        return device.gatt.connect();
      })
      .then(server => {
        console.log('Getting Device Information Service...');
        return server.getPrimaryService(0x2220);
      })
      .then(service => {
        console.log('Getting Device Information Characteristics...');
        return service.getCharacteristics();
      })
      .then(charArray => {
        for (let char of charArray) {
          if (char.properties.write === true && char.uuid.startsWith("00002222")) {
            writeChar = char;
          }
          if (char.properties.read === true && char.uuid.startsWith("00002221")) {
            readChar = char;
          }
          if (char.uuid.startsWith("00002223")) {
            disconnectChar = char;
          }
        }

        let encoder = new TextEncoder('utf-8');
        let randomNumGen = encoder.encode(Math.random().toString()); //to convert it into a UInt8Array for nacl to sign
        let messageHash = hash(randomNumGen);
        let signature = sign.detached(messageHash, keyPair.secretKey);
        let stringEnder = encoder.encode("//");
        let sendMsg = concatenate(Uint8Array, messageHash, signature, keyPair.publicKey, stringEnder);
        let numOfChunks = Math.ceil(sendMsg.byteLength / 20);
        var msgChunks = splitByMaxLength(sendMsg, numOfChunks);

        var prevPromise = Promise.resolve();
        for (let i=0; i< numOfChunks; i++) {
           prevPromise = prevPromise.then(function() {
             return writeChar.writeValue(msgChunks[i]).then(function() {
               if (i === numOfChunks-1) {
                    wait(5000);
                 var prevWhilePromise = Promise.resolve();
                 for (let j=0; j< 3; j++) {      //for now just reading 3 times for testing purposes
                    prevWhilePromise = prevWhilePromise.then(function() {
                      return readChar.readValue().then(value => {
                        var valueRec = new Uint8Array(value.buffer);
                        if(valueRec[0] === 49) {
                          context.setState({verifiedState:true})
                        }
                       })
                    })
                  }
                }
             })
          })
        }

  }).catch(error => {
    console.log("this is err" + error);
  })
}

  skipConnection() {
    this.props.history.push('/');
  }

  render() {
    return (
      <div className="mfa-container">
        {this.state.verifiedState ?
          <p> {successfulConnectMessage} </p>
          :
          <div>
          <p> {connectMessage} </p>
          <Button type="primary" className="mfa-button" onClick={this.startConnection.bind(this)}> {connectButton} </Button>
          <Button type="default" onClick={this.skipConnection.bind(this)}> {absentButton} </Button>
          </div>
        }
      </div>
    );
  }
}


function wait(ms){
   var start = new Date().getTime();
   var end = start;
   while(end < start + ms) {
     end = new Date().getTime();
  }
}


function splitByMaxLength(sendMsg, numOfChunks) {
    let chunks = new Array(numOfChunks);
    let i, j, k;
    for (i=0; i<numOfChunks; i++) {
      chunks[i] = new Uint8Array(20);
      for (j=0, k=i*20; j<20 && k<i*20+20; j++, k++) {
          chunks[i][j] = sendMsg[k];
      }
    }
    return chunks;
}

function dis(disconnectChar) {
    disconnectChar.writeValue(new Uint8Array([1]));
}

function concatenate(resultConstructor, ...arrays) {
    let totalLength = 0;
    for (let arr of arrays) {
        totalLength += arr.byteLength;
    }
    let result = new resultConstructor(totalLength);
    let offset = 0;
    for (let arr of arrays) {
        result.set(arr, offset);
        offset += arr.byteLength;
    }
    return result;
}

export default MFA;
