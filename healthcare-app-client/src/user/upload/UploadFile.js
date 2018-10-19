import React, { Component } from 'react';
import { Upload, Button, Icon } from 'antd';
import { API_BASE_URL, AUTH_TOKEN } from '../../constants/index.js'

class UploadFile extends Component {
  state = {
    fileList: []
  }

  handleChange = (info) => {
    let fileList = info.fileList;

    fileList = fileList.map((file) => {
      if (file.response) {
        // Component will show file.url as link
        var url = file.response.message.split("/")
        url = url[url.length-1]
        if (url.includes(".mp4"))
          file.url = "http://localhost:3000/downloadVideo/" + url
        else if (url.includes(".jpg") || url.includes(".png"))
          file.url = "http://localhost:3000/downloadImage/" + url
      }
      return file;
    });

      fileList = fileList.filter((file) => {
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
