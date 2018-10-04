import React, { Component } from 'react';
import {
    Link,
    withRouter
} from 'react-router-dom';
import { Button, Layout, Table, Icon } from 'antd';
import './Manageusers.css';

class AddUserButton extends Component {
  render() {
    return (
      <Link to="/adduser">
        <Button type="primary" icon="user-add" size="default">Add user</Button>
      </Link>
    );
  }
}

class Administrator_manage_users extends Component {
    constructor(props) {
        super(props);
        this.state = {
            user: this.props.currentUser,
            isLoading: false
        }
    }

    render() {
        const { Header, Content } = Layout;

        const users_columns = [{
          title: 'Name',
          dataIndex: 'name',
          key: 'name',
        }, {
          title: 'NRIC',
          dataIndex: 'nric',
          key: 'nric',
        }, {
          title: 'Role',
          dataIndex: 'role',
          key: 'role',
        }, {
          title: 'Phone number',
          dataIndex: 'phone',
          key: 'phone',
        }, {
          title: 'Email',
          dataIndex: 'email',
          key: 'email',
        }, {
          title: 'Remove?',
          key: 'remove',
          render: text => <a href="#">
                            <Icon type="user-delete" />
                          </a>
        }];

        const requests_columns = [{
          title: 'Name',
          dataIndex: 'name',
          key: 'name',
        }, {
          title: 'NRIC',
          dataIndex: 'nric',
          key: 'nric',
        }, {
          title: 'Role',
          dataIndex: 'role',
          key: 'role',
        }, {
          title: 'Phone number',
          dataIndex: 'phone',
          key: 'phone',
        }, {
          title: 'Email',
          dataIndex: 'email',
          key: 'email',
        }, {
          title: 'Action',
          key: 'action',
          render: text =>
            <span>
              <a href="#">
                <Icon type="check-circle-o" />
              </a>
              <a href="#">
                <Icon type="close-circle" />
              </a>
            </span>
        }];

        return (
              <Layout className="userlayout">
                <Content>
                  <div className="usertitle">
                    Users&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<AddUserButton />
                  </div>
                  <Table columns={users_columns} />
                  <div className="requesttitle">Requests</div>
                  <Table columns={requests_columns} />
                </Content>
              </Layout>
        );
    }
}

export default Administrator_manage_users;
