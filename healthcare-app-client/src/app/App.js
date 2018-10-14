import React, { Component } from 'react';
import './App.css';
import {
  Route,
  withRouter,
  Switch
} from 'react-router-dom';

import { getCurrentUser } from '../util/APIUtils';
import { AUTH_TOKEN } from '../constants';

import Login from '../user/login/Login';
import MFA from '../user/login/MFA';
import Signup from '../user/signup/Signup';
import CreateRecord from '../user/records/CreateRecord';
import MyRecords from '../user/records/MyRecords';
import AllRecords from '../user/records/AllRecords';
import UploadFile from '../user/upload/UploadFile';
import AppHeader from '../common/AppHeader';
// import Therapist_mypatients from '../user/therapist/Mypatients';
import Administrator_logs from '../user/administrator/Logs';
import Administrator_manage_users from '../user/administrator/Manageusers';
import Administrator_add_user from '../user/administrator/Adduser';
import Researcher_generate_data from '../user/researcher/Generatedata';
import External_upload_database from '../user/external_partner/Uploaddatabase';
import LoadingIndicator from '../common/LoadingIndicator';
import PrivateRoute from '../common/PrivateRoute';
import PatientRoute from '../common/PatientRoute';
import TherapistRoute from '../common/TherapistRoute';
import ResearcherRoute from '../common/ResearcherRoute';
import AdministratorRoute from '../common/AdministratorRoute';
import ExternalPartnerRoute from '../common/ExternalPartnerRoute';
import NotFound from '../common/NotFound';
import Chart from '../util/CSVtoGraph'

import { Layout, notification } from 'antd';
const { Content } = Layout;

const Home = () => (
  <div>
    <h1>Welcome to the Healthcare App Webpage!</h1>
  </div>
)

class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      currentUser: null,
      isAuthenticated: false,
      isLoading: false
    }
    this.handleLogout = this.handleLogout.bind(this);
    this.handleLogin = this.handleLogin.bind(this);
    this.loadCurrentUser = this.loadCurrentUser.bind(this);

    notification.config({
      placement: 'topRight',
      top: 70,
      duration: 3,
    });
  }

  handleLogout(redirectTo="/login", notificationType="success", description="You're successfully logged out.") {
    localStorage.removeItem(AUTH_TOKEN);

    this.setState({
      currentUser: null,
      isAuthenticated: false
    });

    this.props.history.push("/");

    notification[notificationType]({
      message: 'Healthcare App',
      description: description,
    });
  }

  handleLogin() {
    notification.success({
      message: 'Healthcare App',
      description: "You're successfully logged in.",
    });
    this.loadCurrentUser();
    this.props.history.push("/mfa");
  }

  loadCurrentUser() {
    this.setState({
      isLoading: true
    });
    getCurrentUser()
    .then(response => {
      this.setState({
        currentUser: response,
        isAuthenticated: true,
        isLoading: false
      });
    }).catch(error => {
      this.setState({
        isLoading: false
      });
    });
  }

  componentWillMount() {
    this.loadCurrentUser();
  }

  render() {
    if(this.state.isLoading) {
      return <LoadingIndicator />
    }

    if(this.state.isAuthenticated) {
      return (
          <Layout className="app-container">
            <AppHeader isAuthenticated={this.state.isAuthenticated}
              currentUser={this.state.currentUser}
              onLogout={this.handleLogout} />

            <Content className="app-content">
              <div className="container">
                <Switch>
                  <Route exact path="/" component={Home}>
                  </Route>
                  <Route path="/login"
                    render={(props) => <Login onLogin={this.handleLogin} {...props} />}></Route>
                  <Route path="/signup" component={Signup}></Route>
                  <PrivateRoute authenticated={this.state.isAuthenticated} path="/mfa" component={MFA}></PrivateRoute>
                  <AdministratorRoute authenticated={this.state.isAuthenticated} role={this.state.currentUser.role} path="/logs" component={Administrator_logs}></AdministratorRoute>
                  <AdministratorRoute authenticated={this.state.isAuthenticated} role={this.state.currentUser.role} path="/manageusers" component={Administrator_manage_users}></AdministratorRoute>
                  <AdministratorRoute authenticated={this.state.isAuthenticated} role={this.state.currentUser.role} path="/adduser" component={Administrator_add_user}></AdministratorRoute>
                  <ResearcherRoute authenticated={this.state.isAuthenticated} role={this.state.currentUser.role} path="/generatedata" component={Researcher_generate_data}></ResearcherRoute>
                  <ExternalPartnerRoute authenticated={this.state.isAuthenticated} role={this.state.currentUser.role} path="/uploaddatabase" component={External_upload_database}></ExternalPartnerRoute>
                  <PatientRoute authenticated={this.state.isAuthenticated} role={this.state.currentUser.role} path="/records/:role/:nric" component={MyRecords}></PatientRoute>
                  <PatientRoute authenticated={this.state.isAuthenticated} role={this.state.currentUser.role} path="/create" component={CreateRecord}></PatientRoute>
                  <PatientRoute authenticated={this.state.isAuthenticated} role={this.state.currentUser.role} path="/all" component={AllRecords}></PatientRoute>
                  <TherapistRoute authenticated={this.state.isAuthenticated} role={this.state.currentUser.role} path="/upload" component={UploadFile}></TherapistRoute>
                  <Route path="/chart" component={Chart}></Route>

                  <Route component={NotFound}></Route>
                </Switch>
              </div>
            </Content>
          </Layout>
      );
    } else {
      return (
          <Layout className="app-container">
            <AppHeader isAuthenticated={this.state.isAuthenticated}
              currentUser={this.state.currentUser}
              onLogout={this.handleLogout} />

            <Content className="app-content">
              <div className="container">
                <Switch>
                  <Route exact path="/" component={Home}>
                  </Route>
                  <Route path="/login"
                    render={(props) => <Login onLogin={this.handleLogin} {...props} />}></Route>
                  <Route path="/signup" component={Signup}></Route>
                  <Route component={NotFound}></Route>
                </Switch>
              </div>
            </Content>
          </Layout>
      );
    }
  }
}

export default withRouter(App);
