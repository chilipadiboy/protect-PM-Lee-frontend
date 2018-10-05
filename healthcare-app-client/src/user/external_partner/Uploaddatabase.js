import React, { Component } from 'react';
import { Form, Select, Upload, Input, Button, Layout, Icon } from 'antd';
import './Uploaddatabase.css';

const FormItem = Form.Item;
const Option = Select.Option;

class UploadButton extends Component {
  render() {
    return (
      <div>
        <Button type="primary" htmlType="submit" icon="file-text" size="default">Upload</Button>
      </div>
    );
  }
}

class UploadDataForm extends Component {
  handleSubmit = (e) => {
    e.preventDefault();
    this.props.form.validateFields((err, values) => {
      if (!err) {
        console.log('Received values of form: ', values);
      }
    });
  }

  normFile = (e) => {
    console.log('Upload event:', e);
    if (Array.isArray(e)) {
      return e;
    }
    return e && e.fileList;
  }

  render() {
      const { Header, Content } = Layout;
      const { getFieldDecorator } = this.props.form;
      const formItemLayout = {
        labelCol: { span: 8 },
        wrapperCol: { span: 14 },
      };

      return (
            <Layout className="layout">
              <Header>
                <div className="title">Upload Data</div>
              </Header>
              <Content>
                <Form onSubmit={this.handleSubmit}>
                  <FormItem
                    {...formItemLayout}
                    label="Patients database file"
                    >
                    {getFieldDecorator('upload', {
                      valuePropName: 'file',
                      getValueFromEvent: this.normFile,
                    })(
                      <Upload name="logo" action="/upload.do" listType="picture">
                        <Button>
                          <Icon type="search" /> Click to select file
                        </Button>
                      </Upload>
                    )}
                  </FormItem>
                  <FormItem
                    {...formItemLayout}
                    label="Therapists database file"
                    >
                    {getFieldDecorator('upload', {
                      valuePropName: 'file',
                      getValueFromEvent: this.normFile,
                    })(
                      <Upload name="logo" action="/upload.do" listType="picture">
                        <Button>
                          <Icon type="search" /> Click to select file
                        </Button>
                      </Upload>
                    )}
                  </FormItem>
                  <FormItem
                    {...formItemLayout}
                    label="Therapist-patient relationship database file"
                    >
                    {getFieldDecorator('upload', {
                      valuePropName: 'file',
                      getValueFromEvent: this.normFile,
                    })(
                      <Upload name="logo" action="/upload.do" listType="picture">
                        <Button>
                          <Icon type="search" /> Click to select file
                        </Button>
                      </Upload>
                    )}
                  </FormItem>
                  <FormItem
                    {...formItemLayout}
                    label="Patients' records and documents database file"
                    >
                    {getFieldDecorator('upload', {
                      valuePropName: 'file',
                      getValueFromEvent: this.normFile,
                    })(
                      <Upload name="logo" action="/upload.do" listType="picture">
                        <Button>
                          <Icon type="search" /> Click to select file
                        </Button>
                      </Upload>
                    )}
                  </FormItem>
                  <FormItem
                    {...formItemLayout}
                    label="Therapists' documents database file"
                    >
                    {getFieldDecorator('upload', {
                      valuePropName: 'file',
                      getValueFromEvent: this.normFile,
                    })(
                      <Upload name="logo" action="/upload.do" listType="picture">
                        <Button>
                          <Icon type="search" /> Click to select file
                        </Button>
                      </Upload>
                    )}
                  </FormItem>
                  <FormItem
                    {...formItemLayout}
                    label="Patients' consent database file"
                    >
                    {getFieldDecorator('upload', {
                      valuePropName: 'file',
                      getValueFromEvent: this.normFile,
                    })(
                      <Upload name="logo" action="/upload.do" listType="picture">
                        <Button>
                          <Icon type="search" /> Click to select file
                        </Button>
                      </Upload>
                    )}
                  </FormItem>
                  <FormItem
                    {...formItemLayout}
                    label="Therapists' consent database file"
                    >
                    {getFieldDecorator('upload', {
                      valuePropName: 'file',
                      getValueFromEvent: this.normFile,
                    })(
                      <Upload name="logo" action="/upload.do" listType="picture">
                        <Button>
                          <Icon type="search" /> Click to select file
                        </Button>
                      </Upload>
                    )}
                    <br /><br />
                  </FormItem>
                  <FormItem
                    wrapperCol={{ span: 8, offset: 4 }}
                  >
                    <UploadButton />
                  </FormItem>
                </Form>
              </Content>
            </Layout>
      );
  }
}

class External_upload_database extends Component {
    constructor(props) {
        super(props);
        this.state = {
            user: this.props.currentUser,
            isLoading: false
        }
    }

    render() {
      const WrappedForm = Form.create()(UploadDataForm);

      return <WrappedForm />;
    }
}

export default External_upload_database;
