import React, { Component } from 'react';
import './Login.css';
import { Link } from 'react-router-dom';
import { AUTH_TOKEN } from '../../constants';
import { Mutation } from 'react-apollo';
import gql from 'graphql-tag';

import { Form, Input, Button, Icon } from 'antd';
const FormItem = Form.Item;

const LOGIN_MUTATION = gql`
  mutation($nric: String!, $password: String!) {
    login(nric: $nric, password: $password)
  }
`

class Login extends Component {
    render() {
        const AntWrappedLoginForm = Form.create()(LoginForm)
        return (
            <div className="login-container">
                <h1 className="page-title">Login</h1>
                <div className="login-content">
                    <AntWrappedLoginForm onLogin={this.props.onLogin} />
                </div>
            </div>
        );
    }
}

class LoginForm extends Component {
    state = {
      nric: '',
      password: '',
    }

    render() {
        const { nric, password } = this.state
        const { getFieldDecorator } = this.props.form;
        return (
            <Form onSubmit={this.handleSubmit} className="login-form">
                <FormItem>
                    {getFieldDecorator('nric', {
                        rules: [{ required: true, message: 'Please input your nric!' }],
                    })(
                    <Input
                        prefix={<Icon type="user" />}
                        size="large"
                        onChange={e => this.setState({ nric: e.target.value })}
                        name="nric"
                        placeholder="NRIC" />
                    )}
                </FormItem>
                <FormItem>
                {getFieldDecorator('password', {
                    rules: [{ required: true, message: 'Please input your Password!' }],
                })(
                    <Input
                        prefix={<Icon type="lock" />}
                        size="large"
                        onChange={e => this.setState({ password: e.target.value })}
                        name="password"
                        type="password"
                        placeholder="Password"  />
                )}
                </FormItem>
                <FormItem>
                <Mutation
                  mutation={LOGIN_MUTATION}
                  variables={{ nric, password }}
                  onCompleted={data => this._confirm(data)}
                >
                {mutation => (
                    <Button type="primary" onClick={mutation} size="large" className="login-form-button">Login</Button>
                )}
                </Mutation>
                    Or <Link to="/signup">register now!</Link>
                </FormItem>
            </Form>
        );
    }

    _confirm = async data => {
      const { token } = data.login
      this._saveUserData(token)
      this.props.onLogin()
    }

    _saveUserData = token => {
      localStorage.setItem(AUTH_TOKEN, token)
    }
}


export default Login;
