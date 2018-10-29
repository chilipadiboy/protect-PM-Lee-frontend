import React, { Component } from 'react';
import {
    Link,
    withRouter
} from 'react-router-dom';
import { matchPath } from 'react-router';
import { getPatients, getPatientPermittedRecords, getUserProfile } from '../../util/APIUtils';
import { Layout, Table, Icon, Button } from 'antd';
import LoadingIndicator  from '../../common/LoadingIndicator';
import './Patientrecords.css';
import NotFound from '../../common/NotFound';
import ServerError from '../../common/ServerError';

class UploadRecordButton extends Component {
  render() {
    return (
      <Link to={ this.props.history.location.pathname + "/uploadrecord"}>
        <Button type="primary" icon="upload" size="default">Upload record</Button>
      </Link>
    );
  }
}

class NewNoteButton extends Component {
  render() {
    return (
      <Link to={ this.props.history.location.pathname + "/newnote"}>
        <Button type="primary" icon="file-add" size="default">New note</Button>
      </Link>
    );
  }
}

class Therapist_patientrecords extends Component {
    constructor(props) {
        super(props);
        this.state = {
            patdata: [],
            mydata: [],
            patient: null,
            verifyPatient: false,
            isLoading: true
        }
        this.loadPatientRecords = this.loadPatientRecords.bind(this);
        this.loadPatientProfile = this.loadPatientProfile.bind(this);
        this.loadMyNotes = this.loadMyNotes.bind(this);
        this.verifyPatient = this.verifyPatient.bind(this);
    }
    // To implement: load notes of current user for the patient
    loadMyNotes() {

    }

    loadPatientRecords(pat_nric) {
        this.setState({
            isLoading: true
        });

        getPatientPermittedRecords(pat_nric)
        .then(patdata =>
          this.setState({ patdata }))
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

    verifyPatient() {
        this.setState({
            isLoading: true
        });

        getPatients()
        .then(response => {
                            const patdata = [];

                            for (var i = 0; i < response.content.length; i++) {
                                patdata[i] = response.content[i].treatmentId.patient;
                            }

                            var i = patdata.indexOf(this.state.patient.nric);

                            if (i >= 0) {
                                this.setState({ verifyPatient: true });
                            } else {
                                this.setState({ verifyPatient: false });
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

    // getUserProfile not working, wait for it to be implemented?, need to verify that patient IS assigned
    // to the current therapist
    loadPatientProfile(pat_nric) {
        this.setState({
            isLoading: true
        });

        getUserProfile(pat_nric)
        .then(response => {
            this.setState({
                patient: response,
                isLoading: false
            });
        }).catch(error => {
            if(error.status === 404) {
                this.setState({
                    notFound: true,
                    isLoading: false
                });
            } else {
                this.setState({
                    serverError: true,
                    isLoading: false
                });
            }
        });
    }

    componentDidMount() {
        const match = matchPath(this.props.history.location.pathname, {
          path: '/mypatients/:nric',
          exact: true,
          strict: false
        });
        const pat_nric = match.params.nric;
        this.loadPatientProfile(pat_nric);
        this.verifyPatient();
        this.loadPatientRecords(pat_nric);
        this.setState({
            isLoading: false
        });
    }


    componentWillReceiveProps(nextProps) {
        if(this.props.match.params.nric !== nextProps.match.params.nric) {
            this.loadPatientProfile(nextProps.match.params.nric);
            this.verifyPatient();
            this.loadPatientRecords(nextProps.match.params.nric);
            this.setState({
                isLoading: false
            });
        }
    }
    // Change the columns? Add links to the docs?
    render() {
        const { Header, Content } = Layout;
        const patcolumns = [{
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

        const mycolumns = [{
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
        // Add the buttons in
        if (!this.state.isLoading) {
          if (this.state.verifyPatient) {
            return (
              <div className="patient-data">
                {  this.state.patient ? (
                    <Layout className="layout">
                      <Content>
                        <div style={{ background: '#ECECEC' }}>
                          <div className="name">&nbsp;&nbsp;{this.state.patient.name}</div>
                          <div className="subtitle">&nbsp;&nbsp;&nbsp;&nbsp;NRIC: {this.state.patient.nric}
                          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Phone Number: {this.state.patient.phone}</div>
                          <br />
                        </div>
                        <div className="title">
                          Patient's Records & Notes
                        </div>
                        <Table dataSource={this.state.patdata.content} columns={patcolumns} />
                        <div className="title">
                          My Notes
                        </div>
                        <Table dataSource={this.state.mydata.content} columns={mycolumns} />
                      </Content>
                    </Layout>
                  ): null
                }
              </div>
            );
          } else {
              return (
                <NotFound />
              );
          }
        } else {
           return null;
        }
    }
}

export default Therapist_patientrecords;
