import React, { Component } from 'react';
import { login, getServerSignature, verifyTagSignature } from '../../util/APIUtils';
import './Login.css';
import { Link } from 'react-router-dom';
import { AUTH_TOKEN } from '../../constants';
import {convertBase64StrToUint8Array, convertUint8ArrayToStr, wait, splitByMaxLength,
dis, concatenate, getTagSigAndMsg, writeUid, readUid, disconUid} from '../../util/MFAUtils';
import { Form, Input, Button, Icon, Select, notification, Spin } from 'antd';
import {sign, hash} from 'tweetnacl';

const FormItem = Form.Item;
const Option = Select.Option;


var encoder = new TextEncoder('utf-8');
var writeChar, readChar, disconnectChar, deviceConnected;
var valueRecArray = [];


class Login extends Component {
    render() {
        const AntWrappedLoginForm = Form.create()(LoginForm)
        return (
            <div className="login-container">
                <h1 className="page-title">Login</h1>
                <div className="login-content">
                    <AntWrappedLoginForm onLogin={this.props.onLogin} />
                </div>
            </div>
        );
    }
}

class LoginForm extends Component {
    constructor(props) {
        super(props);
        this.state = {
          isLoading: false,
          nric: '',
          password: '',
          role: ''
        }
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    startConnection() {
       valueRecArray = [];
       let context = this;
       var ivStr;
       this.setState({isLoading:true});
       navigator.bluetooth.requestDevice({
         filters: [ {services:[0x2220]}, {name:'ifs'},]
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
           const loginRequest = {
               nric: context.state.nric.value,
               password: context.state.password.value,
               role: context.state.role.value,
           };
           getServerSignature(loginRequest)
           .then(response => {
               ivStr = response.iv;
               let encrypted = convertBase64StrToUint8Array(response.encrypted);
               let iv = convertBase64StrToUint8Array(ivStr);
               let stringEnder = encoder.encode("//");
               let sendMsg = concatenate(Uint8Array, iv, encrypted, stringEnder);
               let numOfChunks = Math.ceil(sendMsg.byteLength / 20);
               var msgChunks = splitByMaxLength(sendMsg, numOfChunks);
               var prevPromise = Promise.resolve();
               for (let i=0; i< numOfChunks; i++) {
                  prevPromise = prevPromise.then(function() {
                    return writeChar.writeValue(msgChunks[i]).then(function() {
                      console.log(msgChunks[i]);
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
                                     let encryptedMsg = getTagSigAndMsg(valueRecArray);
                                     let ivMsg = {iv: ivStr};
                                     let reqToSend =  Object.assign({}, encryptedMsg, ivMsg, loginRequest);
                                     console.log(reqToSend);
                                     verifyTagSignature(reqToSend)
                                      .then(response => {
                                        localStorage.setItem(AUTH_TOKEN, response.sessionId);
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
                }).catch(error => {
                    context.setState({isLoading: false});
                    if(error.status === 401) {
                        notification.error({
                            message: 'Healthcare App',
                            description: 'Your NRIC/Password/Role is/are incorrect. Please try again!'
                        });
                    } else {
                        notification.error({
                            message: 'Healthcare App',
                            description: error.message || 'Sorry! Something went wrong. Please try again!'
                        });
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
        const loginRequest = {
            nric: encodeURIComponent(this.state.nric.value),
            password: encodeURIComponent(this.state.password.value),
            role: encodeURIComponent(this.state.role.value)
        };
        login(loginRequest)
        .then(response => {
            localStorage.setItem(AUTH_TOKEN, response.sessionId);
            this.props.onLogin();
        }).catch(error => {
            if(error.status === 401) {
                notification.error({
                    message: 'Healthcare App',
                    description: 'Your NRIC/Password/Role is/are incorrect. Please try again!'
                });
            } else {
                notification.error({
                    message: 'Healthcare App',
                    description: error.message || 'Sorry! Something went wrong. Please try again!'
                });
            }
        });
    }

    render() {
        return (
           <Spin spinning={this.state.isLoading}>
            <Form onSubmit={this.handleSubmit} className="login-form">
                <FormItem>
                    <Input
                        prefix={<Icon type="user" />}
                        size="large"
                        name="nric"
                        value={this.state.nric.value}
                        onChange={(event) => {this.handleInputChange(event)}}
                        placeholder="NRIC" />
                </FormItem>
                <FormItem>
                    <Input
                        prefix={<Icon type="lock" />}
                        size="large"
                        name="password"
                        type="password"
                        onChange={(event) => {this.handleInputChange(event)}}
                        placeholder="Password"  />
                </FormItem>
                <FormItem
                    label="Role">
                    <Select
                        size="large"
                        name="role"
                        autoComplete="off"
                        onChange={(value) => this.setState({
                            role : {
                                value: value
                            }})}
                        placeholder="Select your role">
                        <Option value="patient">Patient</Option>
                        <Option value="therapist">Therapist</Option>
                        <Option value="researcher">Researcher</Option>
                        <Option value="external_partner">External Partner</Option>
                        <Option value="administrator">Administrator</Option>
                    </Select>
                </FormItem>
                <FormItem>
                    <Button type="primary" htmlType="submit" className="login-form-button">Login without your tag</Button>
                    <Button type="primary" className="mfa-button" size="large" onClick={this.startConnection.bind(this)}> Connect Your Tag To Log In </Button>
                </FormItem>
            </Form>
            </Spin>
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

export default Login;
