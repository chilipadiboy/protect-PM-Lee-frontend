import React, { Component } from 'react';
import { Layout, Upload, Button, Icon } from 'antd';
import { API, AUTH_TOKEN } from '../../constants/index.js';
import './Uploaddatabase.css';

class External_upload_database extends Component {

  constructor(props) {
    super(props);
    this.state = {
      filelist: []
    }
    this.handleChange = this.handleChange.bind(this);
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

  render() {
    const { Header, Content } = Layout;
    const props = {
      action: API + "/external/upload/" + this.state.fileList,
      headers: {
        SessionId: localStorage.getItem(AUTH_TOKEN),
        enctype: "multipart/form-data"
      },
      withCredentials: 'include',
      onChange: this.handleChange,
      multiple: false
    };
    return (
      <Layout className="layout">
      <Header>
      <div className="title">Upload Database</div>
      </Header>
      <Content>
      <Upload {...props} fileList={this.state.fileList}>
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
