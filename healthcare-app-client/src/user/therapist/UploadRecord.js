import React, { Component } from 'react';
import { Form, Input, Upload, Button, Icon, Select, notification, Spin  } from 'antd';
import { createRecord, createRecordSignature, verifyCreateRecordTagSignature } from '../../util/APIUtils';
import { convertBase64StrToUint8Array, convertUint8ArrayToStr, wait, splitByMaxLength,
dis, concatenate, getTagSigAndMsg, writeUid, readUid, disconUid} from '../../util/MFAUtils';
import './UploadRecord.css';

const FormItem = Form.Item;
var encoder = new TextEncoder('utf-8');
var writeChar, readChar, disconnectChar, deviceConnected;
var valueRecArray = [];
const Option = Select.Option;
const firstData = ['illness', 'reading'];
const secondData = {
  illness: ['all', 'allergy', 'asthma', 'back pain', 'bronchitis', 'cancer', 'cataracts', 'caries', 'chickenpox', 'cold', 'depression',
  'eating disorders', 'gingivitis', 'gout', 'haemorrhoids', 'headches and migraines', 'heart disease', 'high blood cholestrol', 'hypertension',
'panic attack', 'obsessive compulsive disorder', 'schizophrenia', 'stroke', 'urinary'],
  reading: ['blood pressure'],
};

class Therapist_uploadrecord extends Component {
  constructor(props) {
        super(props);
        this.state = {
          type: '',
          subtype: '',
          title: '',
          patientIC: '',
          isLoading: false,
          selectedFilelist: [],
          data: {
            startData: secondData[firstData[0]],
            nextData: secondData[firstData[0]][0],
          }
        }
        this.handleFirstDataChange = this.handleFirstDataChange.bind(this);
        this.onSecondDataChange = this.onSecondDataChange.bind(this);
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

    handleFirstDataChange = (value) => {
    this.setState({
      type: {
        value: value
      },
      data: {
        startData: secondData[value],
        nextData: secondData[value][0],
      }
    });
  }

  onSecondDataChange = (value) => {
    this.setState({
      subtype: {
        value: value
      },
      data: {
        startData: this.state.data.startData,
        nextData: value,
      }
    });
  }

    handleSubmit(event) {
        event.preventDefault();
        if (this.state.selectedFileList.length<=0) {
          notification["error"]({
           message: 'Healthcare App',
           description: 'Please select a file first!',
         });
         return;
        }
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
      valueRecArray = [];
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
             return server.getPrimaryService(0x2220);
           })
           .then(service => {
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
                       for (let j=0; j< 7; j++) {
                          prevWhilePromise = prevWhilePromise.then(function() {
                            return readChar.readValue().then(value => {
                              let valueRec = new Uint8Array(value.buffer);
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
                                if (j===6) {
                                  dis(disconnectChar);
                                  let encryptedMsg = getTagSigAndMsg(valueRecArray);
                                  let ivMsg = {iv: ivStr};
                                  let reqToSend =  Object.assign({}, encryptedMsg, ivMsg);
                                  verifyCreateRecordTagSignature(createRecordRequest, uploadedFile, reqToSend)
                                   .then(response => {
                                     context.setState({isLoading: false});
                                     notification.success({
                                         message: 'Healthcare App',
                                         description: "Record created!",
                                     });
                                     context.props.history.push("/all");
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
                 }).catch(error => {
                   context.setState({isLoading: false});
                   notification.error({
                      message: 'Healthcare App',
                      description: error.message || 'Sorry! Something went wrong. Please try again!'
                  });
                })
            }
          }).catch(error => {
             context.setState({isLoading: false});
             notification.error({
                message: 'Healthcare App',
                description: error.message || 'Sorry! Something went wrong. Please try again!'
          });
        })
       }).catch(error => {
           context.setState({isLoading: false});
           notification.error({
              message: 'Healthcare App',
              description: error.message || 'Sorry! Something went wrong. Please try again!'
        });
       })
    }


    render() {
        return (
            <div className="createRecord-container">
                <h1 className="page-title">Create New Record</h1>
                <div className="createRecord-content">
                <Spin spinning={this.state.isLoading}>
                    <Form onSubmit={this.handleSubmit} className="createRecord-form">
                        <FormItem
                          label="Type">
                          <Select
                              size="large"
                              required="true"
                              name="type"
                              onChange={this.handleFirstDataChange}>
                              {firstData.map(first => <Option key={first}>{first}</Option>)}
                          </Select>
                        </FormItem>
                        <FormItem
                            label="Subtype">
                            <Select
                                size="large"
                                name="subtype"
                                value={this.state.data.nextData}
                                onChange={this.onSecondDataChange}>
                                {this.state.data.startData.map(second => <Option key={second}>{second}</Option>)}
                            </Select>
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
                            <Button type="primary"
                                size="large"
                                className="createRecord-form-button"
                                onClick={this.startConnection.bind(this)}
                                >Connect Patient Tag To Create Record</Button>
                        </FormItem>
                    </Form>
                  </Spin>
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


export default Therapist_uploadrecord;
