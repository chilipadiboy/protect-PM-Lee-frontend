import React, { Component } from 'react';
import { getAllRecords } from '../../util/APIUtils';
import { Layout, Table, Icon } from 'antd';
import { getAvatarColor } from '../../util/Colors';
import LoadingIndicator  from '../../common/LoadingIndicator';
import './AllRecords.css';
import NotFound from '../../common/NotFound';
import ServerError from '../../common/ServerError';

class MyRecords extends Component {
    constructor(props) {
        super(props);
        this.state = {
            data: []
        }
        this.loadAllRecords = this.loadAllRecords.bind(this);
    }

    loadAllRecords() {
        getAllRecords()
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
        this.loadAllRecords();
    }

    render() {
        const columns = [{
          title: 'Record ID',
          dataIndex: 'recordID',
          key: 'recordID',
          defaultSortOrder: 'ascend',
          sorter: (a, b) => a.recordID - b.recordID
        }, {
          title: 'Type',
          dataIndex: 'type',
          key: 'type',
        }, {
          title: 'Subtype',
          dataIndex: 'subtype',
          key: 'subtype',
          filters: [{
            text: 'Blood Pressure',
            value: 'Blood Pressure',
          }, {
            text: 'Diabetes',
            value: 'Diabetes',
          }, {
            text: 'Heart',
            value: 'Heart',
          }],
          onFilter: (value, record) => record.subtype.indexOf(value) === 0
        }, {
          title: 'Title',
          dataIndex: 'title',
          key: 'title',
        }, {
          title: 'Document',
          dataIndex: 'document',
          key: 'document',
        }, {
          title: 'PatientIC',
          dataIndex: 'patientIC',
          key: 'patientIC',
        }];
         return (
            <Table dataSource={this.state.data.content} columns={columns} />
        );
    }
}

export default MyRecords;
