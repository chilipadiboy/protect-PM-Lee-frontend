import React, { Component } from 'react';
import { matchPath } from 'react-router';
import { Layout, notification } from 'antd';
import { downloadFile, downloadImg } from '../../util/APIUtils'

var image = new Image();

class DownloadFile extends Component {
  constructor(props) {
      super(props);
      this.showImage = this.showImage.bind(this);
  }

  showImage(filename) {
    const IMAGE_REGEX = RegExp('^|\.jpg$|\.png$');
    const FILE_REGEX = RegExp('^|\.csv$|\.txt$');
    if (IMAGE_REGEX.test(filename)) {
      downloadImg(filename)
      .then(response => {
        image.src = response
        image.width=500
        image.height=500
        document.body.appendChild(image);
      })
      .catch(error => {
        notification.error({
            message: 'Healthcare App',
            description: error.message || 'Sorry! Something went wrong. Please try again!'
        });
    });
    } else if (FILE_REGEX.test(filename)) {
      downloadFile(filename)
      .then(response => {

      })
      .catch(error => {
        notification.error({
            message: 'Healthcare App',
            description: error.message || 'Sorry! Something went wrong. Please try again!'
        });
      });
    }
  }

  componentDidMount() {
    const match = matchPath(this.props.history.location.pathname, {
      path: '/download/:filename',
      exact: true,
      strict: false
    })
      const filename = match.params.filename;
      this.showImage(filename);
  }

  render() {
    return (
      <Layout className="app-container">
      </Layout>
    );
  }
}

export default DownloadFile;
