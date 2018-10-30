import React, { Component } from 'react';
import { Form, Select, Button, Layout} from 'antd';
import './Generatedata.css';

const FormItem = Form.Item;
const Option = Select.Option;

class GenerateButton extends Component {
  render() {
    return (
      <div>
        <Button type="primary" htmlType="submit" icon="file-text" size="default">Generate</Button>
      </div>
    );
  }
}

class GenerateDataForm extends Component {
  handleSubmit = (e) => {
    e.preventDefault();
    this.props.form.validateFields((err, values) => {
      if (!err) {
        console.log('Received values of form: ', values);
      }
    });
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
                        <Option value="all">All</Option>
                        <Option value="below 13">Below 13</Option>
                        <Option value="from 13 to 18">13 to 18</Option>
                        <Option value="from 19 to 25">19 to 25</Option>
                        <Option value="from 26 to 35">26 to 35</Option>
                        <Option value="from 36 to 55">36 to 55</Option>
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
                        <Option value="all">All</Option>
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
                        <Option value="all">All</Option>
                        <Option value="Central">Central</Option>
                        <Option value="East">East</Option>
                        <Option value="North">North</Option>
                        <Option value="North-East">North-East</Option>
                        <Option value="North-West">North-East</Option>
                        <Option value="South">South</Option>
                        <Option value="South-West">South-West</Option>
                        <Option value="West">West</Option>
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
                      <Option value="Allergy">Allergy</Option>
                      <Option value="Asthma">Asthma</Option>
                      <Option value="Back Pain">Back Pain</Option>
                      <Option value="Blood Cholestrol">Blood Cholestrol</Option>
                      <Option value="Bronchitis">Bronchitis</Option>
                      <Option value="Cancer">Cancer</Option>
                      <Option value="Cataracts">Cataracts</Option>
                      <Option value="Caries">Caries</Option>
                      <Option value="Chickenpox">Chickenpox</Option>
                      <Option value="Cold">Cold</Option>
                      <Option value="Depression">Depression</Option>
                      <Option value="Diabetes">Diabetes</Option>
                      <Option value="Eating Disorders">Eating Disorders</Option>
                      <Option value="Gingivitis">Gingivitis</Option>
                      <Option value="Gout">Gout</Option>
                      <Option value="Haemorrhoids">Haemorrhoids</Option>
                      <Option value="Headaches and Migraines">Headaches & Migraines</Option>
                      <Option value="Heart Disease">Heart Disease</Option>
                      <Option value="Hypertension">Hypertension</Option>
                      <Option value="Panic Attack">Panic Attack</Option>
                      <Option value="Obsessive Compulsive Disorder">Obsessive Compulsive</Option>
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
