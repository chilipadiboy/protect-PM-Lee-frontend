import React, { Component } from 'react';
import { createRecord } from '../../util/APIUtils';
import './CreateTherapistDoc.css';

import { Form, Input, Button, notification } from 'antd';

const FormItem = Form.Item;

class CreateTherapistDoc extends Component {
  constructor(props) {
        super(props);
        this.state = {
          recordID: '',
          type: 'document',
          subtype: '',
          title: '',
          document: '',
          patientIC: ''
        }
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
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
            recordID: this.state.recordID.value,
            type: this.state.type.value,
            subtype: this.state.subtype.value,
            title: this.state.title.value,
            document: this.state.document.value,
            patientIC: this.state.patientIC.value
        };
        createRecord(createRecordRequest)
        .then(response => {
            notification.success({
                message: 'Healthcare App',
                description: "Record created!",
            });
            this.props.history.push("/");
        }).catch(error => {
            notification.error({
                message: 'Healthcare App',
                description: error.message || 'Sorry! Something went wrong. Please try again!'
            });
        });
    }

    render() {
        return (
            <div className="createRecord-container">
                <h1 className="page-title">Create New Record</h1>
                <div className="createRecord-content">
                    <Form onSubmit={this.handleSubmit} className="createRecord-form">
                    <FormItem
                      label="RecordID">
                      <Input
                          size="large"
                          name="recordID"
                          autoComplete="off"
                          value={this.state.recordID.value}
                          onChange={(event) => {this.handleInputChange(event)}}  />
                    </FormItem>
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
                            label="Document">
                            <Input
                                size="large"
                                name="document"
                                autoComplete="off"
                                value={this.state.document.value}
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

export default CreateTherapistDoc;
