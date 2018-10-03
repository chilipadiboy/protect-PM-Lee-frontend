import React, { Component } from 'react';
import { Layout, Table, Icon } from 'antd';
import './Mypatients.css';


class Therapist_mypatients extends Component {
    constructor(props) {
        super(props);
        this.state = {
            user: this.props.currentUser,
            isLoading: false
        }
    }

    render() {
        const columns = [{
          title: 'Name',
          dataIndex: 'name',
          key: 'name',
        }, {
          title: 'NRIC',
          dataIndex: 'nric',
          key: 'nric',
        }, {
          title: 'Gender',
          dataIndex: 'gender',
          key: 'gender',
        }, {
          title: 'Age',
          dataIndex: 'age',
          key: 'age',
        }, {
          title: 'Start date',
          dataIndex: 'start',
          key: 'start',
        }, {
          title: 'Next appointment',
          dataIndex: 'next_appt',
          key: 'next_appt',
        }, {
          title: 'Phone number',
          dataIndex: 'phone',
          key: 'phone',
        }, {
          title: 'Health issues',
          dataIndex: 'health_issues',
          key: 'health_issues',
        }, {
          title: 'Allergies',
          dataIndex: 'allergies',
          key: 'allergies',
        }, {
          title: 'Documents & records',
          dataIndex: 'docs_recs',
          key: 'docs_recs',
          render: text => <a href="#">View, Edit or Create</a>,
        }];

        const data = [{
          key: '1',
          name: 'Bobby Fisher',
          nric: 'S8854321I',
          gender: 'Female',
          age: 30,
          start: '12 Aug 18',
          next_appt: '1 Dec 18',
          phone: '98144819',
          health_issues: 'Diabetes',
          allergies: 'Paracetamol',
        }, {
          key: '2',
          name: 'John Doe',
          nric: 'S7612345J',
          gender: 'Male',
          age: 42,
          start: '10 Aug 17',
          next_appt: '3 Dec 18',
          phone: '87687654',
          health_issues: 'Insomnia',
          allergies: '-',
        }];

        return (
            <Table dataSource={data} columns={columns} />
        );
    }
}

export default Therapist_mypatients;
