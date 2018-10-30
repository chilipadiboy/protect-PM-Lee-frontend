import React, { Component } from 'react';
import {
    Link,
    withRouter
} from 'react-router-dom';
import { matchPath } from 'react-router';
import { getPatients, getPatientPermittedRecords, getPatientProfile,
         getAllTherapistNotes, getCurrentUser, setNotePermission } from '../../util/APIUtils';
import { Layout, Table, Icon, Button, Input, Popconfirm, Form } from 'antd';
import LoadingIndicator  from '../../common/LoadingIndicator';
import './Patientrecords.css';
import NotFound from '../../common/NotFound';
import ServerError from '../../common/ServerError';

// maybe useful:
// <Link to={ this.props.history.location.pathname + "/newnote"}>
//   <Button type="primary" icon="file-add" size="default">New note</Button>
// </Link>


const FormItem = Form.Item;
const EditableContext = React.createContext();

const EditableRow = ({ form, index, ...props }) => (
  <EditableContext.Provider value={form}>
    <tr {...props} />
  </EditableContext.Provider>
);

const EditableFormRow = Form.create()(EditableRow);

class EditableCell extends Component {
  state = {
    editing: false,
  }

  componentDidMount() {
    if (this.props.editable) {
      document.addEventListener('click', this.handleClickOutside, true);
    }
  }

  componentWillUnmount() {
    if (this.props.editable) {
      document.removeEventListener('click', this.handleClickOutside, true);
    }
  }

  toggleEdit = () => {
    const editing = !this.state.editing;
    this.setState({ editing }, () => {
      if (editing) {
        this.input.focus();
      }
    });
  }

  handleClickOutside = (e) => {
    const { editing } = this.state;
    if (editing && this.cell !== e.target && !this.cell.contains(e.target)) {
      this.save();
    }
  }

  save = () => {
    const { record, handleSave } = this.props;
    this.form.validateFields((error, values) => {
      if (error) {
        return;
      }
      this.toggleEdit();
      handleSave({ ...record, ...values });
    });
  }

  render() {
    const { editing } = this.state;
    const {
      editable,
      dataIndex,
      title,
      record,
      index,
      handleSave,
      ...restProps
    } = this.props;
    return (
      <td ref={node => (this.cell = node)} {...restProps}>
        {editable ? (
          <EditableContext.Consumer>
            {(form) => {
              this.form = form;
              return (
                editing ? (
                  <FormItem style={{ margin: 0 }}>
                    {form.getFieldDecorator(dataIndex, {
                      rules: [{
                        required: true,
                        message: `${title} is required.`,
                      }],
                      initialValue: record[dataIndex],
                    })(
                      <Input
                        ref={node => (this.input = node)}
                        onPressEnter={this.save}
                      />
                    )}
                  </FormItem>
                ) : (
                  <div
                    className="editable-cell-value-wrap"
                    style={{ paddingRight: 24 }}
                    onClick={this.toggleEdit}
                  >
                    {restProps.children}
                  </div>
                )
              );
            }}
          </EditableContext.Consumer>
        ) : restProps.children}
      </td>
    );
  }
}

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
        this.handleDelete = this.handleDelete.bind(this);
        this.handleAdd = this.handleAdd.bind(this);
        this.handleSave = this.handleSave.bind(this);
        this.radioOnChange = this.radioOnChange.bind(this);
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

    handleDelete = (key) => {
      // const dataSource = [...this.state.dataSource];
      // this.setState({ dataSource: dataSource.filter(item => item.key !== key) });
      //Handle delete (wait for deletion implementation)
    }

    handleAdd = () => {
      const { count, dataSource } = this.state;
      const newData = {
        key: count,
        name: `Edward King ${count}`,
        age: 32,
        address: `London, Park Lane no. ${count}`,
      };
      this.setState({
        dataSource: [...dataSource, newData],
        count: count + 1,
      });
    }

    handleSave = (row) => {
      const newData = [...this.state.dataSource];
      const index = newData.findIndex(item => row.key === item.key);
      const item = newData[index];
      newData.splice(index, 1, {
        ...item,
        ...row,
      });
      this.setState({ dataSource: newData });
    }

    radioOnChange(e) {
      const checked = e.target.checked;
      if (checked) {
        const notePermissionRequest = {
            noteID: e.target.value,
            isVisibleToPatient: "true"
        };
        setNotePermission(notePermissionRequest)
        .then(response => {
            window.location.reload();
        }).catch(error => {
            notification.error({
                message: 'Healthcare App',
                description: error.message || 'Sorry! Something went wrong. Please try again!'
            });
            e.target.checked = "false";
        });
      } else {
        const notePermissionRequest = {
            noteID: e.target.value,
            isVisibleToPatient: "false"
        };
        setNotePermission(notePermissionRequest)
        .then(response => {
            window.location.reload();
        }).catch(error => {
            notification.error({
                message: 'Healthcare App',
                description: error.message || 'Sorry! Something went wrong. Please try again!'
            });
            e.target.checked = "false";
        });
      }
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
          title: 'File',
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
          title: 'Content',
          dataIndex: 'noteContent'
        }];

        const mynotescolumns = [{
          title: 'Note ID',
          dataIndex: 'noteID',
          key: 'noteID',
          defaultSortOrder: 'ascend',
          sorter: (a, b) => a.noteID - b.noteID
        }, {
          title: 'Content',
          dataIndex: 'noteContent'
        }];

        const testcolumns = [{
          title: 'Note ID',
          dataIndex: 'noteID',
          key: 'noteID',
          defaultSortOrder: 'ascend',
          sorter: (a, b) => a.noteID - b.noteID
        }, {
          title: 'Content',
          dataIndex: 'noteContent',
          width: '30%',
          editable: true
        }, {
          title: 'Give Patient Consent-to-view?',
          dataIndex: 'consent',
          render: (text, row) => <Checkbox value={row.noteID} onChange={radioOnChange}></Checkbox> //Implement variable defaultChecked
        }, {
          title: 'Delete?',
          dataIndex: 'delete',
          render: (text, record) => {
            return (
              this.state.mynotes.length >= 1
                ? (
                  <Popconfirm title="Sure to delete?" onConfirm={() => this.handleDelete(record.key)}>
                    <a href="javascript:;">Delete</a>
                  </Popconfirm>
                ) : null
            );
          }
        }];

        const components = {
          body: {
            row: EditableFormRow,
            cell: EditableCell,
          },
        };

        const columns = mynotescolumns.map((col) => {
          if (!col.editable) {
            return col;
          }
          return {
            ...col,
            onCell: record => ({
              record,
              editable: col.editable,
              dataIndex: col.dataIndex,
              title: col.title,
              handleSave: this.handleSave,
            }),
          };
        });

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
                      <Button onClick={this.handleAdd} type="primary" style={{ marginBottom: 16 }}>
                        New note
                      </Button>
                    </div>
                    <Table dataSource={this.state.mynotes} columns={mynotescolumns} />
                    <Table
                      components={components}
                      rowClassName={() => 'editable-row'}
                      bordered
                      dataSource={this.state.mynotes}
                      columns={mynotescolumns}
                    />
                  </Content>
                </Layout>
              ): null
            }
          </div>
        );
    }
}

export default Therapist_patientrecords;
