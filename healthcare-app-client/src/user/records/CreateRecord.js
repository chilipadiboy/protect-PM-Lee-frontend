import React, { Component } from 'react';
import { Form, Input, Upload, Button, Icon, notification } from 'antd';
import { createRecord } from '../../util/APIUtils';
import './CreateRecord.css';

const FormItem = Form.Item;

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
                    </Form>
                </div>
            </div>
        );
    }
  }

export default CreateRecord;
