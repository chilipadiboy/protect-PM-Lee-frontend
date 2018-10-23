import React, { Component } from 'react';
import { Upload, Button, Icon, Form, Select} from 'antd';
import { API, API_BASE_URL, AUTH_TOKEN } from '../../constants/index.js';
import {sign, hash} from 'tweetnacl';
import {getServerFileDataSignature} from '../../util/APIUtils';

const connectToPatientMsg = "Connect to patient's tag to upload file for patient";

const FormItem = Form.Item;
const Option = Select.Option;
var formData = new FormData();


const messageHashLength = 64;
const signatureLength = 64;
const writeUid = "00002222";
const readUid = "00002221";
const disconUid = "00002223";

var encoder = new TextEncoder('utf-8');
var writeChar, readChar, disconnectChar, deviceConnected;
var valueRecArray = [];
>>>>>>> add bluetooth connect for upload front end - part one

class UploadFile extends Component {

  constructor(props) {
    super(props);
    this.state = {
      fileList: [],
      patientExists: false,
    }
    //this.handleChange = this.handleChange.bind(this);
  }

  startConnection() {
       let context = this;
       let patientNric;
       this.setState({isLoading:true});
       navigator.bluetooth.requestDevice({
         filters: [ {services:[0x2220]},]
       })
         .then(device => {
           deviceConnected = device;
           patientNric = device.name;
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
          formData.append("file", this.state.fileList[0]);
         getServerFileDataSignature(formData)
         .then(response => {
           console.log(response);
         })

       });


  }

  handleChange = (info) => {
    let fileList = info.fileList;

    fileList = fileList.map((file) => {
      if (file.response) {
        // Component will show file.url as link
        var url = file.response.message.split("/")
        url = url[url.length-1]
        if (url.includes(".mp4"))
          file.url = this.props.history.location.pathname + "/downloadVideo/" + url
        else if (url.includes(".jpg") || url.includes(".png"))
          file.url = this.props.history.location.pathname + "/downloadImage/" + url
      }
      return file;
    });

      fileList = fileList.filter((file) => {
        return true;
      });

    this.setState({ fileList });
  }


  render() {
    const props = {
      action: API + "/file/upload",
      headers: {
        SessionId: localStorage.getItem(AUTH_TOKEN),
        enctype: "multipart/form-data"
      },
    //  withCredentials: 'include',
      //onChange: this.handleChange,
      multiple: false,
      beforeUpload: (file) => {
        this.setState({
          fileList: [file],
        });
        return false;
      },
      fileList: this.state.fileList,

    };


    return (
      <div>
      <Upload {...props} fileList={this.state.fileList}>
        <Button>
          <Icon type="upload" /> Select File
        </Button>
      </Upload>
      <Button
        disabled={this.state.fileList.length === 0}
        onClick={this.startConnection.bind(this)}
      >
      {connectToPatientMsg}
      </Button>



      </div>
    );
  }
}

export default UploadFile;
