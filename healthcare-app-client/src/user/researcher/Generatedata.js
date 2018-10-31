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
      console.log(values)
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
                    label="Age"
                    labelCol={{ span: 4 }}
                    wrapperCol={{ span: 8 }}
                  >
                    {getFieldDecorator('age', {
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
                        <Option value="central">Central</Option>
                        <Option value="east">East</Option>
                        <Option value="north">North</Option>
                        <Option value="north-east">North-East</Option>
                        <Option value="north-west">North-East</Option>
                        <Option value="south">South</Option>
                        <Option value="south-west">South-West</Option>
                        <Option value="west">West</Option>
                      </Select>
                    )}
                  </FormItem>
                  <FormItem
                    label="Type"
                    labelCol={{ span: 4 }}
                    wrapperCol={{ span: 8 }}
                  >
                    {getFieldDecorator('type', {
                      rules: [{ required: true, message: 'Please select a type of health data!' }],
                    })(
                      <Select
                        placeholder="Select an option"
                      >
                      <Option value="illness">Illness</Option>
                      <Option value="reading">Reading</Option>
                      </Select>
                    )}
                  </FormItem>
                  <FormItem
                    label="Subtype"
                    labelCol={{ span: 4 }}
                    wrapperCol={{ span: 8 }}
                  >
                    {getFieldDecorator('subtype', {
                      rules: [{ required: true, message: 'Please select a type of health data!' }],
                    })(
                      <Select
                        placeholder="Select an option"
                      >
                      <Option value="all">All</Option>
                      <Option value="allergy">Allergy</Option>
                      <Option value="asthma">Asthma</Option>
                      <Option value="back pain">Back Pain</Option>
                      <Option value="high blood cholestrol">High Blood Cholestrol</Option>
                      <Option value="bronchitis">Bronchitis</Option>
                      <Option value="cancer">Cancer</Option>
                      <Option value="cataracts">Cataracts</Option>
                      <Option value="caries">Caries</Option>
                      <Option value="chickenpox">Chickenpox</Option>
                      <Option value="cold">Cold</Option>
                      <Option value="depression">Depression</Option>
                      <Option value="diabetes">Diabetes</Option>
                      <Option value="eating disorders">Eating Disorders</Option>
                      <Option value="gingivitis">Gingivitis</Option>
                      <Option value="gout">Gout</Option>
                      <Option value="haemorrhoids">Haemorrhoids</Option>
                      <Option value="headaches and migraines">Headaches & Migraines</Option>
                      <Option value="heart disease">Heart Disease</Option>
                      <Option value="hypertension">Hypertension</Option>
                      <Option value="panic attack">Panic Attack</Option>
                      <Option value="obsessive compulsive disorder">Obsessive Compulsive</Option>
                      </Select>
                    )}
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
