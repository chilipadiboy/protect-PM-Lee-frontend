import React, { Component } from 'react';
import { Layout, Upload, Button, Icon } from 'antd';
import { API, AUTH_TOKEN } from '../../constants/index.js';
import { externalUpload } from '../../util/APIUtils'
import './Uploaddatabase.css';

class External_upload_database extends Component {

  constructor(props) {
    super(props);
    this.state = {
      filelist: [],
    }
    this.handleChange = this.handleChange.bind(this);
    this.customRequest = this.customRequest.bind(this);
  }

  handleChange = (info) => {
    let fileList = info.fileList;

    fileList = fileList.map((file) => {
      if (file.response) {
        // Component will show file.url as link
        file.url = file.response.fileDownloadUri
      }
      return file;
    });

      fileList = fileList.filter((file) => {
        return true;
      });

    this.setState({ fileList });
  }

  customRequest = ({ onSuccess, onError, file }) => {
      setTimeout(() => {
          externalUpload(file)
            .then(() => {
              onSuccess(null, file);
            })
            .catch(() => {
              console.log("error")
            });
      }, 100);
  }

  render() {
    const { Header, Content } = Layout;
    const props = {
      action: API + "/external/upload/users",
      headers: {
        SessionId: localStorage.getItem(AUTH_TOKEN),
        enctype: "multipart/form-data"
      },
      onChange: this.handleChange,
      withCredentials: "include",
    };
    return (
      <Layout className="layout">
      <Header>
      <div className="title">Upload Database</div>
      </Header>
      <Content>
      <Upload {...props} fileList={this.state.filelist}>
        <Button>
          <Icon type="upload" /> Upload
        </Button>
      </Upload>
      </Content>
      </Layout>
    );
  }
}

export default External_upload_database;
