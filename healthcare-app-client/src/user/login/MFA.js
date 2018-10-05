import React, { Component } from 'react';
import './MFA.css';
import {Button, Spin} from 'antd';
import {sign, hash} from 'tweetnacl';

const connectMessage = "Please connect your bluetooth tag for multi-factor authentication!";
const successfulConnectMessage = "Connection was successful! User verified!";
const tryAgainMessage ="Please try again!";
const connectButton = "Connect";
const absentButton = "No Tag";
const messageHashLength = 64;
const signatureLength = 64;
const pubKeyLength = 32;
const keyPair = sign.keyPair();
//this should be retrieved from the server as
// {publicKey: Uint8Array(32), secretKey: Uint8Array(32)}
var encoder = new TextEncoder('utf-8');
var writeChar, readChar, disconnectChar, deviceConnected;
var valueRecArray = [];
var tagMessageHash = new Uint8Array(64);
var tagSignature = new Uint8Array(64);
var tagPublicKey = new Uint8Array(32);


class MFA extends Component {
  constructor(props) {
    super(props);
    this.state = {
      verifiedState: false,
      loading: false,
      attemptBefore: false
    }
  }

  startConnection() {
   let context = this;
    navigator.bluetooth.requestDevice({
      filters: [ {services:[0x2220]} ]
    })
      .then(device => {
        deviceConnected = device;
        context.setState({loading:true});
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
             if (!deviceConnected.gatt.connected) {
               deviceConnected.gatt.connect();
             }
             return writeChar.writeValue(msgChunks[i]).then(function() {
               if (i === numOfChunks-1) {
                    wait(5000);
                 var prevWhilePromise = Promise.resolve();
                 for (let j=0; j< 8; j++) {
                    prevWhilePromise = prevWhilePromise.then(function() {
                      return readChar.readValue().then(value => {
                        let valueRec = new Uint8Array(value.buffer);
                        for (let i=0; i<value.buffer.byteLength; i++) {
                          valueRecArray.push(valueRec[i]);
                        }
                        let ack = "ACK" + j;
                        ack = encoder.encode(ack);
                        return writeChar.writeValue(ack).then(function() {
                          if (j==7) {
                            dis(disconnectChar);
                            if (verifyTag()) {
                              context.setState({verifiedState:true, loading:false})
                            } else {
                              context.setState({verifiedState:false, loading:false, attemptBefore:true})
                            }
                          }
                       })
                    })
                  })
                }
              }
             }).catch(err => console.log(err));
          })
        }

  }).catch(error => {
    console.log(error);
  })
}

  skipConnection() {
    this.props.history.push('/');
  }

  render() {
    return (
      <div className="mfa-container">
         <Spin spinning={this.state.loading}>
          {this.state.verifiedState  ?
            <p> {successfulConnectMessage} </p>
             :
            <div>
              <div>
              {this.state.attemptBefore ?
                <p> {tryAgainMessage}</p>
                : <p>{connectMessage}</p>
               }
              </div>
              <Button type="primary" className="mfa-button" onClick={this.startConnection.bind(this)}> {connectButton} </Button>
              <Button type="default" onClick={this.skipConnection.bind(this)}> {absentButton} </Button>
            </div>
          }
         </Spin>
      </div>
    );
  }
}

function verifyTag() {
  let i,j;

  for(i=0, j=0; i<messageHashLength && j<messageHashLength; i++, j++) {
    tagMessageHash[j] = valueRecArray[i];
  }

  for(i=i, j=0; i<messageHashLength+signatureLength && j<signatureLength; i++, j++) {
    tagSignature[j] = valueRecArray[i];
  }

  for(i=i, j=0; i<messageHashLength+signatureLength+pubKeyLength && j<pubKeyLength; i++, j++) {
    tagPublicKey[j] = valueRecArray[i];
  }

  let isTagVerified = sign.detached.verify(tagMessageHash, tagSignature, tagPublicKey);
  tagMessageHash = new Uint8Array(64);
  tagSignature = new Uint8Array(64);
  tagPublicKey = new Uint8Array(32);
  return isTagVerified;

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
