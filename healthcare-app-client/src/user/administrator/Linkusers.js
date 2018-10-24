import React, { Component } from 'react';
import { signup } from '../../util/APIUtils';
import { Form, Input, Button, Select, notification } from 'antd';
import './Linkusers.css';

const FormItem = Form.Item;
const Option = Select.Option;

class Administrator_link_users extends Component {
  constructor(props) {
        super(props);
        this.state = {
          therapist: '',
          patients: '',
        }
        this.handleSubmit = this.handleSubmit.bind(this);
    }

  handleSubmit(event) {
      event.preventDefault();
      const linkuserRequest = {
          nric: this.state.therapists.value,
          name: this.state.patients.value,
      };
      signup(linkuserRequest) //to change when api is up
      .then(response => {
          notification.success({
              message: 'Healthcare App',
              description: "You've successfully assigned the users!",
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
          <div className="linkuser-container">
              <h1 className="page-title">Therapist -> Patient(s)</h1>
              <div className="linkuser-content">
                  <Form onSubmit={this.handleSubmit} className="linkuser-form">
                      <FormItem
                          label="Therapist">
                          <Select placeholder="Select a therapist"
                              size="large"
                              name="therapist"
                              autoComplete="off"
                              onChange={(value) => this.setState({
                                  therapist : {
                                      value: value
                                  }})}>
                              <Option value="therapist1">Therapist 1</Option>
                              <Option value="therapist2">Therapist 2</Option>
                              <Option value="therapist3">Therapist 3</Option>
                              <Option value="therapist4">Therapist 4</Option>
                              <Option value="therapist5">Therapist 5</Option>
                          </Select>
                      </FormItem>
                      <FormItem
                          label="Patients">
                          <Select mode="multiple" placeholder="Select patients"
                              size="large"
                              name="patients"
                              autoComplete="off"
                              onChange={(value) => this.setState({
                                  patients : {
                                      value: value
                                  }})}>
                              <Option value="patient1">Patient 1</Option>
                              <Option value="patient2">Patient 2</Option>
                              <Option value="patient3">Patient 3</Option>
                              <Option value="patient4">Patient 4</Option>
                              <Option value="patient4">Patient 5</Option>
                          </Select>
                      </FormItem>
                      <FormItem>
                          <Button type="primary" icon="arrow-right"
                              htmlType="submit"
                              size="large"
                              className="linkuser-form-button"
                              >Assign</Button>
                      </FormItem>
                  </Form>
              </div>
          </div>
      );
  }
}

export default Administrator_link_users;
