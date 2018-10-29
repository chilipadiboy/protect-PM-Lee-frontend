import React, { Component } from 'react';
import { Form, Input, Upload, Button, Icon, notification } from 'antd';
import { createRecord, createRecordSignature, verifyCreateRecordTagSignature } from '../../util/APIUtils';
import './CreateRecord.css';

const FormItem = Form.Item;


const messageHashLength = 64;
const signatureLength = 64;
const writeUid = "00002222";
const readUid = "00002221";
const disconUid = "00002223";

var encoder = new TextEncoder('utf-8');
var writeChar, readChar, disconnectChar, deviceConnected;
var valueRecArray = [];

class CreateRecord extends Component {
  constructor(props) {
        super(props);
        this.state = {
          type: '',
          subtype: '',
          title: '',
          patientIC: '',
          selectedFilelist: []
        }
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.beforeUpload = this.beforeUpload.bind(this);
    }

    handleInputChange(event) {
        const target = event.target;
        const inputName = target.name;
        const inputValue = target.value;

        this.setState({
            [inputName] : {
                value: inputValue
            }
        });
    }

    handleSubmit(event) {
        event.preventDefault();
        const createRecordRequest = {
            type: this.state.type.value,
            subtype: this.state.subtype.value,
            title: this.state.title.value,
            patientIC: encodeURIComponent(this.state.patientIC.value)
        };
        const uploadedFile = this.state.selectedFileList[0]
        createRecord(createRecordRequest, uploadedFile)
        .then(response => {
            notification.success({
                message: 'Healthcare App',
                description: "Record created!",
            });
            this.props.history.push("/all");
        }).catch(error => {
            console.log(error)
            notification.error({
                message: 'Healthcare App',
                description: error.message || 'Sorry! Something went wrong. Please try again!'
            });
        });
    }

    beforeUpload = (file) => {
      this.setState({
          selectedFileList: [file],
        });
      return false;
    };

    startConnection() {
         let context = this;
         let ivStr;
         const createRecordRequest = {
             type: this.state.type.value,
             subtype: this.state.subtype.value,
             title: this.state.title.value,
             patientIC: encodeURIComponent(this.state.patientIC.value)
         };
         const uploadedFile = this.state.selectedFileList[0];
         let patientNric;
         this.setState({isLoading:true});
         navigator.bluetooth.requestDevice({
           filters: [ {services:[0x2220]},]
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
               if (char.properties.write === true && char.uuid.startsWith(writeUid)) {
                 writeChar = char;
               }
               if (char.properties.read === true && char.uuid.startsWith(readUid)) {
                 readChar = char;
               }
               if (char.uuid.startsWith(disconUid)) {
                 disconnectChar = char;
               }
             }
           createRecordSignature(createRecordRequest, uploadedFile)
           .then(response => {
            ivStr = response.iv;
             console.log(response);
             let combined = convertBase64StrToUint8Array(response.combined);
             let signature = convertBase64StrToUint8Array(response.signature);
             let iv = convertBase64StrToUint8Array(ivStr);
             let stringEnder = encoder.encode("//");
             let sendMsg = concatenate(Uint8Array, combined, signature, stringEnder);
             let numOfChunks = Math.ceil(sendMsg.byteLength / 20);
             var msgChunks = splitByMaxLength(sendMsg, numOfChunks);
             var prevPromise = Promise.resolve();
             for (let i=0; i< numOfChunks; i++) {
               prevPromise = prevPromise.then(function() {
                 return writeChar.writeValue(msgChunks[i]).then(function() {
                   if (i === numOfChunks-1) {
                     wait(11000);
                       var prevWhilePromise = Promise.resolve();
                       for (let j=0; j< 8; j++) {
                          prevWhilePromise = prevWhilePromise.then(function() {
                            return readChar.readValue().then(value => {
                              let valueRec = new Uint8Array(value.buffer);
                              console.log(valueRec);
                              if (valueRec[0]===48 && valueRec[1]===48 && j===0) {
                                context.setState({isLoading: false});
                                dis(disconnectChar);
                                openNotificationError(0);
                              }
                              if (valueRec[0]===33 && valueRec[1]===33) {
                                context.setState({isLoading: false});
                                dis(disconnectChar);
                                openNotificationError(1);
                              }
                              for (let i=0; i<value.buffer.byteLength; i++) {
                                valueRecArray.push(valueRec[i]);
                              }
                              let ack = "ACK" + j;
                              ack = encoder.encode(ack);
                              return writeChar.writeValue(ack).then(function() {
                                if (j===7) {
                                  dis(disconnectChar);
                                  let encryptedMsg = getTagSigAndMsg();
                                  let ivMsg = {iv: ivStr};
                                  let reqToSend =  Object.assign({}, encryptedMsg, ivMsg);
                                  verifyCreateRecordTagSignature(createRecordRequest, uploadedFile, reqToSend)
                                   .then(response => {

                                     context.setState({isLoading: false});
                                     context.props.onLogin();
                                   }).catch(error => {
                                     context.setState({isLoading: false});
                                     notification.error({
                                         message: 'Healthcare App',
                                         description: error.message || 'Sorry! Something went wrong. Please try again!'
                                     });
                                   })
                                }
                              })
                            })
                          })
                        }
                      }
                   })
                 })
               }
         })
       }).catch(error => {
         context.setState({isLoading: false});
         if (!deviceConnected.gatt.connected) {
           notification.error({
               message: 'Healthcare App',
               description: 'Device disconnected!'
           });
         } else {
           notification.error({
               message: 'Healthcare App',
               description: error.message || 'Sorry! Something went wrong. Please try again!'
           });
         }
       })
     }


    render() {
        return (
            <div className="createRecord-container">
                <h1 className="page-title">Create New Record</h1>
                <div className="createRecord-content">
                    <Form onSubmit={this.handleSubmit} className="createRecord-form">
                        <FormItem
                          label="Type">
                          <Input
                              size="large"
                              name="type"
                              autoComplete="off"
                              value={this.state.type.value}
                              onChange={(event) => {this.handleInputChange(event)}}  />
                        </FormItem>
                        <FormItem
                            label="Subtype">
                            <Input
                                size="large"
                                name="subtype"
                                autoComplete="off"
                                value={this.state.subtype.value}
                                onChange={(event) => {this.handleInputChange(event)}}  />
                        </FormItem>
                        <FormItem
                            label="Title">
                            <Input
                                size="large"
                                name="title"
                                autoComplete="off"
                                value={this.state.title.value}
                                onChange={(event) => {this.handleInputChange(event)}} />
                        </FormItem>
                        <FormItem
                            label="PatientIC">
                            <Input
                                size="large"
                                name="patientIC"
                                autoComplete="off"
                                value={this.state.patientIC.value}
                                onChange={(event) => {this.handleInputChange(event)}} />
                        </FormItem>
                        <FormItem
                            label="Document">
                            <Upload beforeUpload={this.beforeUpload} fileList={this.state.selectedFileList}>
                              <Button>
                                <Icon type="upload" /> Upload
                              </Button>
                            </Upload>
                        </FormItem>
                        <FormItem>
                            <Button type="primary"
                                htmlType="submit"
                                size="large"
                                className="createRecord-form-button"
                                >Create Record</Button>
                        </FormItem>
                        <Button type="primary"
                            htmlType="submit"
                            size="large"
                            className="createRecord-form-button"
                            onClick={this.startConnection.bind(this)}
                            >Connect Patient Tag</Button>
                    </Form>
                </div>
            </div>
        );
    }
  }

  function openNotificationError(type) {
    if (type===0) {
      notification["error"]({
       message: 'Healthcare App',
       description: 'Connection timed out',
     });
    } else {
      notification["error"]({
       message: 'Healthcare App',
       description: 'Failed to identify you, please try again.',
     });
    }
  }

  function convertBase64StrToUint8Array(str) {
    var binary_string =  window.atob(str);
    var len = binary_string.length;
    var bytes = new Uint8Array(len);
    for (var i = 0; i < len; i++)        {
        bytes[i] = binary_string.charCodeAt(i);
    }
    return bytes;
  }

  function convertUint8ArrayToStr(arr) {
    let base64String = btoa(String.fromCharCode(...arr));
    return base64String;
  }


  function getTagSigAndMsg() {
    let i,j;
    let encryptedMsg = new Uint8Array(128);
    let tagMessageHash = new Uint8Array(64);
    let tagSignature = new Uint8Array(64);
    let tagPublicKey = new Uint8Array(32);

    for(i=0; i<messageHashLength+signatureLength; i++) {
       encryptedMsg[i] = valueRecArray[i];
    }
    let encryptedStr = convertUint8ArrayToStr(encryptedMsg);
    return {encryptedString: encryptedStr};
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

export default CreateRecord;
