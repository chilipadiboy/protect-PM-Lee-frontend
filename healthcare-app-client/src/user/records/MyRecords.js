import React, { Component } from 'react';
import { matchPath } from 'react-router';
import { getUserRecords } from '../../util/APIUtils';
import { Table } from 'antd';
import './MyRecords.css';

class MyRecords extends Component {
    constructor(props) {
        super(props);
        this.state = {
            data: []
        }
        this.loadUserRecords = this.loadUserRecords.bind(this);
    }

    loadUserRecords(nric, role) {
        getUserRecords(nric, role)
        .then(data =>
          this.setState({ data }))
        .catch(error => {
            if(error.status === 404) {
                this.setState({
                    notFound: true,
                });
            } else {
                this.setState({
                    serverError: true,
                });
            }
        });
    }

    componentDidMount() {
        const match = matchPath(this.props.history.location.pathname, {
          path: '/records/:role/:nric',
          exact: true,
          strict: false
        })
        const nric = match.params.nric;
        const role = match.params.role;
        this.loadUserRecords(nric, role);
    }

    render() {
        const columns = [{
          title: 'Record ID',
          dataIndex: 'recordID',
          key: 'recordID',
        }, {
          title: 'Type',
          dataIndex: 'type',
          key: 'type',
        }, {
          title: 'Subtype',
          dataIndex: 'subtype',
          key: 'subtype',
        }, {
          title: 'Title',
          dataIndex: 'title',
          key: 'title',
        }, {
          title: 'Document',
          dataIndex: 'document',
          key: 'document',
          render: text => {
            var url = text.split("/")
            url = url[url.length-1]
            if (url.includes(".mp4"))
              text = "/downloadVideo/" + url
            else if (url.includes(".jpg") || url.includes(".png"))
              text = "/downloadImage/" + url
            else if (url.includes(".txt") || url.includes(".csv"))
              text = "/downloadFile/" + url

            return <a href={text}>{url}</a>
          }
        }];
         return (
            <Table dataSource={this.state.data.content} columns={columns} />
        );
    }
}

export default MyRecords;
