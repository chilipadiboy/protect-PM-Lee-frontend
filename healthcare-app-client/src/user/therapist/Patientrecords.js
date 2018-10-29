import React, { Component } from 'react';
import {
    Link,
    withRouter
} from 'react-router-dom';
import { matchPath } from 'react-router';
import { getPatients, getPatientPermittedRecords, getPatientProfile, getAllTherapistNotes, getCurrentUser } from '../../util/APIUtils';
import { Layout, Table, Icon, Button } from 'antd';
import LoadingIndicator  from '../../common/LoadingIndicator';
import './Patientrecords.css';
import NotFound from '../../common/NotFound';
import ServerError from '../../common/ServerError';

class Therapist_patientrecords extends Component {
    constructor(props) {
        super(props);
        this.state = {
            patrecords: [],
            mynotes: [],
            othernotes: [],
            patient: null,
            currentUser: null,
            isLoading: false
        }
        this.getCurrentTherapist = this.getCurrentTherapist.bind(this);
        this.loadPatientRecords = this.loadPatientRecords.bind(this);
        this.loadPatientProfile = this.loadPatientProfile.bind(this);
        this.loadNotes = this.loadNotes.bind(this);
    }

    getCurrentTherapist() {
        this.setState({
            isLoading: true
        });

        getCurrentUser()
        .then((response) => {
            this.setState({
                currentUser: response,
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

    loadNotes(pat_nric) {
      this.setState({
          isLoading: true
      });

      getAllTherapistNotes(pat_nric)
      .then((response) => {
          console.log(response.content);

          const mydata = [];
          const otherdata = [];
          const mynric = this.state.currentUser.nric;
          var me = 0;
          var other = 0;

          for (var i = 0; i < response.content.length; i++) {
              var currentnric = response.content[i].creatorNric;
              if (mynric === currentnric) {
                  mydata[me] = response.content[i];
                  me++;
              } else {
                  otherdata[other] = response.content[i];
                  other++;
              }
          }

          this.setState({
              mynotes: mydata,
              othernotes: otherdata,
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

    loadPatientRecords(pat_nric) {
        this.setState({
            isLoading: true
        });

        getPatientPermittedRecords(pat_nric)
        .then((patdata) => {
            this.setState({ patrecords: patdata.content,
                            isLoading: false });
        })
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

    loadPatientProfile(pat_nric) {
        this.setState({
            isLoading: true
        });

        getPatientProfile(pat_nric)
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
        this.getCurrentTherapist();
        this.loadPatientProfile(pat_nric);
        this.loadPatientRecords(pat_nric);
        this.loadNotes(pat_nric);
    }


    componentWillReceiveProps(nextProps) {
        if(this.props.match.params.nric !== nextProps.match.params.nric) {
            this.getCurrentTherapist();
            this.loadPatientProfile(nextProps.match.params.nric);
            this.loadPatientRecords(nextProps.match.params.nric);
            this.loadNotes(nextProps.match.params.nric);
        }
    }
    // Change the columns? Add links to the docs?
    render() {
        const { Header, Content } = Layout;

        const patcolumns = [{
          title: 'Record ID',
          dataIndex: 'recordID',
          key: 'recordID',
          defaultSortOrder: 'ascend',
          sorter: (a, b) => a.recordID - b.recordID
        }, {
          title: 'Title',
          dataIndex: 'title'
        }, {
          title: 'Type',
          dataIndex: 'type'
        }, {
          title: 'Subtype',
          dataIndex: 'subtype'
        }, {
          title: 'Document',
          dataIndex: 'document'
        }];

        const othernotescolumns = [{
          title: 'Note ID',
          dataIndex: 'noteID',
          key: 'noteID',
          defaultSortOrder: 'ascend',
          sorter: (a, b) => a.noteID - b.noteID
        }, {
          title: 'Written By',
          dataIndex: 'creatorName'
        }, {
          title: 'Document',
          dataIndex: 'noteContent'
        }];

        const mynotescolumns = [{
          title: 'Note ID',
          dataIndex: 'noteID',
          key: 'noteID',
          defaultSortOrder: 'ascend',
          sorter: (a, b) => a.noteID - b.noteID
        }, {
          title: 'Document',
          dataIndex: 'noteContent'
        }];

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
                      Patient's Records &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                      <Link to={ this.props.history.location.pathname + "/uploadrecord"}>
                        <Button type="primary" icon="upload" size="default">Upload record</Button>
                      </Link>
                    </div>
                    <Table dataSource={this.state.patrecords} columns={patcolumns} />
                    <div className="title">
                      Other Therapists' Notes
                    </div>
                    <Table dataSource={this.state.othernotes} columns={othernotescolumns} />
                    <div className="title">
                      My Notes &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                      <Link to={ this.props.history.location.pathname + "/newnote"}>
                        <Button type="primary" icon="file-add" size="default">New note</Button>
                      </Link>
                    </div>
                    <Table dataSource={this.state.mynotes} columns={mynotescolumns} />
                  </Content>
                </Layout>
              ): null
            }
          </div>
        );
    }
}

export default Therapist_patientrecords;
