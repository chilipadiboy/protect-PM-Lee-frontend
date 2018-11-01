import React, { Component } from 'react';
import {
    Link,
    withRouter
} from 'react-router-dom';
import { getMyRecords, giveTherapistPermission,
         removeTherapistPermission, getMyNotes,
         getGivenPermissions, getTherapistNotes,
         getCurrentUser, getAllMyTherapists } from '../../util/APIUtils';
import { Layout, Table, Icon, Button, Select, notification } from 'antd';
import update from 'immutability-helper';
import LoadingIndicator  from '../../common/LoadingIndicator';
import './MyData.css';
import NotFound from '../../common/NotFound';
import ServerError from '../../common/ServerError';


class Patient_mydata extends Component {
    constructor(props) {
        super(props);
        this.state = {
            myrecords: [],
            mytherapists: [],
            mynotes: [],
            therapistsnotes: [],
            myrecordscompleted: false,
            mytherapistscompleted: false,
            mynotescompleted: false,
            mytherapistsnotescompleted: false,
            isLoading: false
        }

        this.loadMyTherapists = this.loadMyTherapists.bind(this);
        this.loadMyRecords = this.loadMyRecords.bind(this);
        this.loadNotes = this.loadNotes.bind(this);
        this.handleSelectChange = this.handleSelectChange.bind(this);

    }

    loadMyTherapists() {
        this.setState({
            isLoading: true
        });

        getAllMyTherapists()
        .then(response => {

            const mytherapists = [];

            for (var i = 0; i < response.content.length; i++) {
                mytherapists[i] = response.content[i].treatmentId.therapist;
            }

            this.setState({
                mytherapists: mytherapists,
                mytherapistscompleted: true,
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

    loadMyRecords() {
      this.setState({
          isLoading: true
      });

      getMyRecords()
      .then((response) => {

          const myrec = [];

          for (var i = 0; i < response.content.length; i++) {
              myrec[i] = response.content[i];
              myrec[i].permittedTherapists = [];
          }

          this.setState({
              myrecords: myrec
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

      getGivenPermissions()
      .then((response) => {

          for (var i = 0; i < response.content.length; i++) {
              const currperm = response.content[i];
              const recid = currperm.recordID;
              const therapistic = currperm.therapistNric;
              for (var j = 0; j < this.state.myrecords.length; j++) {
                  if (recid == this.state.myrecords[j].recordID) {
                        const prevList = this.state.myrecords[j].permittedTherapists;
                        this.setState({ myrecords: update(this.state.myrecords, {[j]: { permittedTherapists: {$set: [...prevList, therapistic]} }}) });
                  }
              }
          }

          this.setState({
              myrecordscompleted: true,
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

    loadNotes() {
      this.setState({
          isLoading: true
      });

      getTherapistNotes()
      .then((response) => {

          const therapnotes = [];

          for (var i = 0; i < response.content.length; i++) {
              therapnotes[i] = response.content[i];
          }

          this.setState({
              therapistsnotes: therapnotes,
              mytherapistsnotescompleted: true
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

      getMyNotes()
      .then((response) => {

          const myNotes = [];

          for (var i = 0; i < response.content.length; i++) {
              myNotes[i] = response.content[i];
          }

          this.setState({
              mynotes: myNotes,
              mynotescompleted: true
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

      this.setState({
          isLoading: false
      });
    }

    handleSelectChange(e) {
      const currvalue = e.target.value;
      console.log(currvalue);
      // if (checked) {
      //   const notePermissionRequest = {
      //       noteID: e.target.value,
      //       isVisibleToPatient: "true"
      //   };
      //   setNotePermission(notePermissionRequest)
      //   .then(response => {
      //
      //   }).catch(error => {
      //       notification.error({
      //           message: 'Healthcare App',
      //           description: error.message || 'Sorry! Something went wrong. Please try again!'
      //       });
      //       e.target.checked = "false";
      //   });
      // } else {
      //   const notePermissionRequest = {
      //       noteID: e.target.value,
      //       isVisibleToPatient: "false"
      //   };
      //   setNotePermission(notePermissionRequest)
      //   .then(response => {
      //
      //   }).catch(error => {
      //       notification.error({
      //           message: 'Healthcare App',
      //           description: error.message || 'Sorry! Something went wrong. Please try again!'
      //       });
      //       e.target.checked = "true";
      //   });
      // }
    }

    componentDidMount() {
        this.loadMyTherapists();
        this.loadMyRecords();
        this.loadNotes();
    }

    // Change the columns? Add links to the docs?
    render() {
        const { Header, Content } = Layout;
        const Option = Select.Option;

        const mytherapistoptions = [];

        for (var i = 0; i < this.state.mytherapists.length; i++) {
            const currentTherapist = this.state.mytherapists[i];
            mytherapistoptions.push(<Option key={currentTherapist}
                                     value={currentTherapist}>{currentTherapist}</Option>);
        }

        const children = [];

        for (let i = 10; i < 36; i++) {
          children.push(<Option key={i.toString(36) + i}>{i.toString(36) + i}</Option>);
        }

        console.log("start");
        console.log(this.state.datacomplete);
        console.log(this.state.mytherapists);
        console.log(this.state.mynotes);
        console.log(this.state.myrecords);
        console.log(mytherapistoptions);
        console.log(this.state.myrecordscompleted && this.state.mynotescompleted && this.state.mytherapists && this.state.mytherapistsnotescompleted);
        console.log("end");

        const reccolumns = [{
          title: 'Record ID',
          dataIndex: 'recordID',
          key: 'recordID',
          align: 'center',
        }, {
          title: 'Title',
          dataIndex: 'title',
          align: 'center',
        }, {
          title: 'Type',
          dataIndex: 'type',
          align: 'center',
        }, {
          title: 'Subtype',
          dataIndex: 'subtype',
          align: 'center',
        }, {
          title: '',
          dataIndex: 'defaultList',
          render: text => ''
        },{
          title: 'Allow therapist(s) to view?',
          dataIndex: 'consent',
          width: '20%',
          align: 'center',
          render: (text, row) =>  <div>
                                      <Select mode="multiple" placeholder="Select therapist(s) to view"
                                      onChange={this.handleSelectChange}
                                      style={{ width: '100%' }}> <Option key={"hi"}>{"hi"}</Option> </Select>
                                  </div>
        }, {
          title: 'File',
          dataIndex: 'document',
          align: 'center',
        }];

        const therapistsnotescolumns = [{
          title: 'Note ID',
          dataIndex: 'noteID',
          key: 'noteID',
          align: 'center',
        }, {
          title: 'Content',
          dataIndex: 'noteContent',
          align: 'center',
          width: '50%'
        }, {
          title: 'Written By',
          dataIndex: 'creatorName',
          align: 'center',
        }];

        const mynotescolumns = [{
          title: 'Note ID',
          dataIndex: 'noteID',
          key: 'noteID',
          align: 'center',
        }, {
          title: 'Content',
          dataIndex: 'noteContent',
          align: 'center',
          width: '50%'
        }, {
          title: 'Action',
          dataIndex: 'edit',
          align: 'center',
          render: (text, row) => <a href={ this.props.history.location.pathname + "/editnote/" + row.noteID }>Edit</a>
        }];

        return (
          <div className="patient-data">
           {  (this.state.myrecordscompleted && this.state.mynotescompleted && this.state.mytherapists && this.state.mytherapistsnotescompleted) ? (
                 <Layout className="layout">
                   <Content>
                     <div className="title">
                       My Records
                     </div>
                     <Table dataSource={this.state.myrecords} columns={reccolumns} />
                     <div className="title">
                       My Therapists' Notes
                     </div>
                     <Table dataSource={this.state.therapistsnotes} columns={therapistsnotescolumns} />
                     <div className="title">
                       My Notes &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                       <Link to={ this.props.history.location.pathname + "/newnote" }>
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

export default Patient_mydata;
