import React, { Component } from 'react';
import { Form, Select, Input, Button, Layout} from 'antd';
import './Generatedata.css';

const FormItem = Form.Item;
const Option = Select.Option;

class GenerateButton extends Component {
  render() {
    return (
      <div>
        <Button type="primary" icon="file-text" size="default">Generate</Button>
      </div>
    );
  }
}

class GenerateDataForm extends Component {
  handleSubmit = (e) => {
    e.preventDefault();
  }

  render() {
      const { Header, Content } = Layout;
      const { getFieldDecorator } = this.props.form;

      return (
            <Layout className="layout">
              <Header>
                <div className="title">Research Data</div>
              </Header>
              <Content>
                <Form onSubmit={this.handleSubmit}>
                  <FormItem
                    label="Age group"
                    labelCol={{ span: 4 }}
                    wrapperCol={{ span: 8 }}
                  >
                    {getFieldDecorator('age group', {
                      rules: [{ required: true, message: 'Please select an age group!' }],
                    })(
                      <Select
                        placeholder="Select an option"
                      >
                        <Option value="below 13">Below 13</Option>
                        <Option value="13 to 18">13 to 18</Option>
                        <Option value="19 to 25">19 to 25</Option>
                        <Option value="26 to 35">26 to 35</Option>
                        <Option value="36 to 55">36 to 55</Option>
                        <Option value="above 55">Above 55</Option>
                      </Select>
                    )}
                  </FormItem>
                  <FormItem
                    label="Gender"
                    labelCol={{ span: 4 }}
                    wrapperCol={{ span: 8 }}
                  >
                    {getFieldDecorator('gender', {
                      rules: [{ required: true, message: 'Please select a gender!' }],
                    })(
                      <Select
                        placeholder="Select an option"
                      >
                        <Option value="male">Male</Option>
                        <Option value="female">Female</Option>
                      </Select>
                    )}
                  </FormItem>
                  <FormItem
                    label="Location"
                    labelCol={{ span: 4 }}
                    wrapperCol={{ span: 8 }}
                  >
                    {getFieldDecorator('location', {
                      rules: [{ required: true, message: 'Please select a location!' }],
                    })(
                      <Select
                        placeholder="Select an option"
                      >
                        <Option value="central">Central</Option>
                        <Option value="north">North</Option>
                        <Option value="south">South</Option>
                        <Option value="east">East</Option>
                        <Option value="west">West</Option>
                      </Select>
                    )}
                  </FormItem>
                  <FormItem
                    label="Type of health data"
                    labelCol={{ span: 4 }}
                    wrapperCol={{ span: 8 }}
                  >
                    {getFieldDecorator('heath datatype', {
                      rules: [{ required: true, message: 'Please select a type of health data!' }],
                    })(
                      <Select
                        placeholder="Select an option"
                      >
                        <Option value="blood pressure">Blood Pressure</Option>
                        <Option value="weight">Weight</Option>
                      </Select>
                    )}
                    <br /><br />
                  </FormItem>
                  <FormItem
                    wrapperCol={{ span: 8, offset: 4 }}
                  >
                    <GenerateButton />
                  </FormItem>
                </Form>
              </Content>
            </Layout>
      );
  }
}

class Researcher_generate_data extends Component {
    constructor(props) {
        super(props);
        this.state = {
            user: this.props.currentUser,
            isLoading: false
        }
    }

    render() {
      const WrappedForm = Form.create()(GenerateDataForm);

      return <WrappedForm />;
    }
}

export default Researcher_generate_data;
