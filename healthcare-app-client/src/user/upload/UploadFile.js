import React, { Component } from 'react';
import { Upload, Button, Icon, notification } from 'antd';
import { API_BASE_URL, AUTH_TOKEN } from '../../constants/index.js'
import { downloadFile } from '../../util/APIUtils'

class UploadFile extends Component {
  state = {
    fileList: [],
    images: []
  }

  handleChange = (info) => {
    let fileList = info.fileList;

    // 2. Read from response and show file link
    fileList = fileList.map((file) => {
      if (file.response) {
        // Component will show file.url as link
        file.url = file.response.message;
      }
      return file;
    });

    // 3. Filter successfully uploaded files according to response from server
    fileList = fileList.filter((file) => {
      if (file.response) {
        downloadFile(file.name)
        .then(response =>
          console.log("here1")
        )
        .catch(error => {
            notification.error({
                message: 'Healthcare App',
                description: error.message || 'Sorry! Something went wrong. Please try again!'
            });
        });
      }
      return true;
    });

    this.setState({ fileList });
  }

  render() {
    const props = {
      action: API_BASE_URL + "/file/upload",
      headers: {
        Authorization: "Bearer " + localStorage.getItem(AUTH_TOKEN),
        enctype: "multipart/form-data"
      },
      onChange: this.handleChange,
      multiple: false
    };
    return (
      <Upload {...props} fileList={this.state.fileList}>
        <Button>
          <Icon type="upload" /> Upload
        </Button>
      </Upload>
    );
  }
}

export default UploadFile;
