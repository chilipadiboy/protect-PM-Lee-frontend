import React, { Component } from 'react';
import { getUserRecords } from '../../util/APIUtils';
import { Layout, Table, Icon } from 'antd';
import { getAvatarColor } from '../../util/Colors';
import LoadingIndicator  from '../../common/LoadingIndicator';
import './MyRecords.css';
import NotFound from '../../common/NotFound';
import ServerError from '../../common/ServerError';

class MyRecords extends Component {
    constructor(props) {
        super(props);
        this.state = {
            data: []
        }
        this.loadUserRecords = this.loadUserRecords.bind(this);
    }

    loadUserRecords(nric, role) {
        getUserRecords(nric, role.toString)
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
        const nric = this.props.match.params.nric;
        const role = this.props.match.params.role;
        this.loadUserRecords(nric, role);
    }

    componentWillReceiveProps(nextProps) {
        if(this.props.match.params.nric !== nextProps.match.params.nric || this.props.match.params.role !== nextProps.match.params.role) {
            this.loadUserRecords(nextProps.match.params.nric);
            this.loadUserRecords(nextProps.match.params.role);
        }
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
        }];
         return (
            <Table dataSource={this.state.data.content} columns={columns} />
        );
    }
}

export default MyRecords;
