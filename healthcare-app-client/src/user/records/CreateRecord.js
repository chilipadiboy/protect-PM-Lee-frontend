import React, { Component } from 'react';
import { Form, Input, Upload, Button, Icon, notification } from 'antd';
import { createRecord } from '../../util/APIUtils';
import './CreateRecord.css';

const FormItem = Form.Item;

const dummyRequest = ({ file, onSuccess }) => {
  setTimeout(() => {
    onSuccess("ok");
  }, 100);
};

class CreateRecord extends Component {
  constructor(props) {
        super(props);
        this.state = {
          type: '',
          subtype: '',
          title: '',
          patientIC: '',
          selectedFile: '',
          selectedFilelist: []
        }
        this.handleUploadChange = this.handleUploadChange.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.uploadFile = this.uploadFile.bind(this);
    }

    handleUploadChange = (info) => {
      const nextState = {};
      switch (info.file.status) {
        case "uploading":
          nextState.selectedFileList = [info.file];
          break;
          case "done":
          nextState.selectedFile = info.file;
          nextState.selectedFileList = [info.file];
          break;

      default:
        // error or removed
        nextState.selectedFile = null;
        nextState.selectedFileList = [];
      }
      this.setState(() => nextState);
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
            patientIC: this.state.patientIC.value
        };
        const uploadedFile = {
            file: this.state.selectedFile
        };
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

    uploadFile = ({ onSuccess, onError, file }) => {
      const createRecordRequest = {
          type: this.state.type.value,
          subtype: this.state.subtype.value,
          title: this.state.title.value,
          patientIC: this.state.patientIC.value
      };
      setTimeout(() => {
          createRecord(createRecordRequest,file)
            .then((response) => {
              onSuccess("ok");
              notification.success({
                  message: 'Healthcare App',
                  description: "Record created!",
              });
            }).catch(error => {
                console.log(error)
                notification.error({
                    message: 'Healthcare App',
                    description: error.message || 'Sorry! Something went wrong. Please try again!'
                });
            });
      }, 100);
    };

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
                            <Upload customRequest={this.uploadFile} onChange={this.handleUploadChange} fileList={this.state.selectedFileList}>
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
                    </Form>
                </div>
            </div>
        );
    }
  }

export default CreateRecord;
