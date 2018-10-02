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
import Profile from '../user/profile/Profile';
import AppHeader from '../common/AppHeader';
import LoadingIndicator from '../common/LoadingIndicator';
import PrivateRoute from '../common/PrivateRoute';
import NotFound from '../common/NotFound';

import { Layout, notification } from 'antd';
const { Content } = Layout;

const Home = () => (
  <div>
    <h1>Welcome to the Healthcare App Webpage!</h1>
  </div>
)

const Data = () => (
  <img src="https://raw.githubusercontent.com/IFS4205-2018-Sem1-Team1/first-report/master/images/pre_anonymisation.png" alt="homepage"></img>
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
                <Route component={NotFound}></Route>
              </Switch>
            </div>
          </Content>
        </Layout>
    );
  }
}

export default withRouter(App);
