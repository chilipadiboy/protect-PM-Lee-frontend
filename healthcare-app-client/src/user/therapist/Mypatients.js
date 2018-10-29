import React, { Component } from 'react';
import update from 'immutability-helper';
import findIndex from 'lodash.findindex';
import { Layout, Table, Icon } from 'antd';
import { getPatients, getPatientProfile } from '../../util/APIUtils';
import './Mypatients.css';


class Therapist_mypatients extends Component {
    constructor(props) {
        super(props);
        this.state = {
            patients: [],
            isLoading: false
        }

        this.loadPatients = this.loadPatients.bind(this);
    }

    loadPatients() {
        this.setState({
            isLoading: true
        });

        getPatients()
        .then(response => {
                const patdata = [];

                for (var i = 0; i < response.content.length; i++) {
                    var currentnric = response.content[i].treatmentId.patient;

                    patdata[i] = ({ key: i,
                                    nric: currentnric
                                  });
                }

                this.setState({ patients: patdata });

                for (var i = 0; i < response.content.length; i++) {

                    var currentnric = response.content[i].treatmentId.patient;

                    getPatientProfile(currentnric)
                    .then((result) => { var i = findIndex(this.state.patients, ['nric', result.nric]);
                                        this.setState({ patients: update(this.state.patients, {[i]: { name: {$set: result.name},
                                                                                                      phone: {$set: result.phone} }}) });
                                      }
                    ).catch(error => {
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
              }
        )
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
        this.loadPatients();
    }

    render() {

        const columns = [{
          title: 'NRIC',
          dataIndex: 'nric',
          key: 'nric',
        },  {
          title: 'Name',
          dataIndex: 'name',
          key: 'name',
        },  {
          title: 'Phone',
          dataIndex: 'phone',
          key: 'phone',
        },  {
          title: 'Documents & records',
          dataIndex: 'docs_recs',
          key: 'docs_recs',
          render: (text, row) => <a href={ "/mypatients/" + row.nric }>View, Edit or Create</a>,
        }];


        const { Header, Content } = Layout;

        return (
              <Layout className="layout">
                <Header>
                  <div className="title">My Patients</div>
                </Header>
                <Content>
                  <Table dataSource={this.state.patients} columns={columns} />
                </Content>
              </Layout>
        );
    }
}

export default Therapist_mypatients;
