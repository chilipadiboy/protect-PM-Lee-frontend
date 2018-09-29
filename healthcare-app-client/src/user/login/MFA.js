import React, { Component } from 'react';
import './MFA.css';
import { Button } from 'antd';
import {sign} from 'tweetnacl';

const connectMessage = "Please connect your bluetooth tag for multi-factor authentication!";
const connectButton = "Connect";
const absentButton = "No Tag";
const keyPair = sign.keyPair();
//this should be retrieved from the server as
// {publicKey: Uint8Array(32), secretKey: Uint8Array(64)}

class MFA extends Component {
  constructor(props) {
    super(props);
  }

  startConnection() {
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
        let writeChar, readChar;
        for (let char of charArray) {
          if (char.properties.write === true && char.uuid.startsWith("00002222")) {
            writeChar = char;
          }
          if (char.properties.read === true && char.uuid.startsWith("00002221")) {
            readChar = char;
          }
        }
        let encoder = new TextEncoder('utf-8');
        let randomNumGen = encoder.encode(Math.random().toString()); //to convert it into a UInt8Array for nacl to sign
        console.log(randomNumGen);
        let sendMsg = sign(randomNumGen, keyPair.secretKey);
        let numOfChunks = Math.ceil(sendMsg.byteLength / 20);
        var msgChunks = splitByMaxLength(sendMsg, numOfChunks);
        let promise = writeChar.writeValue(msgChunks[0]);
        for (let i=1; i<numOfChunks; i++) {
          promise = promise.then(() => new Promise((resolve, reject) => {
          // Reject promise if the device has been disconnected.
          // Write chunk to the characteristic and resolve the promise.
           writeChar.writeValue(msgChunks[i]).
              then(resolve).
              catch(reject);
          }));
       }
     })
      .then(value => {
        console.log(value);
      })
  }



  skipConnection() {
    this.props.history.push('/');
  }

  render() {
    return (
      <div className="mfa-container">
        <p> {connectMessage} </p>
        <Button type="primary" className="mfa-button" onClick={this.startConnection}> {connectButton} </Button>
        <Button type="default" onClick={this.skipConnection.bind(this)}> {absentButton} </Button>
      </div>
    );
  }
}

function splitByMaxLength(sendMsg, numOfChunks) {
    let chunks = new Array(numOfChunks);
    let i, j, k;
    for (i=0; i<numOfChunks; i++) {
      chunks[i] = new Uint8Array(20);
      for (j=0, k=i*20; j<20, k<i*20+20; j++, k++) {
          chunks[i][j] = sendMsg[k];
      }
    }
    return chunks;
    //console.log(chunks);
  }


export default MFA;
