import React, { Component } from 'react';
import { Form, Upload, Button, Layout, Icon } from 'antd';
import { AUTH_TOKEN, API } from '../../constants/index.js'
import './Uploaddatabase.css';

const FormItem = Form.Item;

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
    const props = {
      action: API + "",
      headers: {
        SessionId: localStorage.getItem(AUTH_TOKEN),
        enctype: "multipart/form-data"
      },
      onChange: this.handleChange,
      multiple: false
    };
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
                    label="Hospital database file"
                    >
                    {getFieldDecorator('upload', {
                      valuePropName: 'file',
                      getValueFromEvent: this.normFile,
                    })(
                      <Upload {...props} name="logo" action="/upload.do" listType="picture">
                        <Button>
                          <Icon type="search" /> Click to select file
                        </Button>
                      </Upload>
                    )}
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
