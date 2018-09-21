import React, { Component } from 'react'
import { AUTH_TOKEN } from '../constants'
import { Mutation } from 'react-apollo'
import gql from 'graphql-tag'

const LOGIN_MUTATION = gql`
  mutation($nric: String!, $password: String!) {
    login(nric: $nric, password: $password)
  }
`

class Login extends Component {
  state = {
    nric: '',
    password: ''
  }

  render() {
    const { login, nric, name, email, phone, address, age, gender, password } = this.state
    return (
      <div>
        <h4 className="mv3">{'Login'}</h4>
        <div className="flex flex-column">
          <input
            value={nric}
            onChange={e => this.setState({ nric: e.target.value })}
            type="text"
            placeholder="Your nric"
          />
          <input
            value={password}
            onChange={e => this.setState({ password: e.target.value })}
            type="password"
            placeholder="Choose a safe password"
          />
        </div>
        <div className="flex mt3">
        <Mutation
          mutation={LOGIN_MUTATION}
          variables={{ nric, password }}
          onCompleted={data => this._confirm(data)}
        >
          {mutation => (
            <div className="pointer mr2 button" onClick={mutation}>
              login
            </div>
          )}
        </Mutation>
          <div
            className="pointer button"
            onClick={() => this.setState({ login: !login })}
          >
            need to create an account?
          </div>
        </div>
      </div>
    )
  }

  _confirm = async data => {
    const { token } = data.login
    this._saveUserData(token)
    this.props.history.push(`/signup`)
  }

  _saveUserData = token => {
    localStorage.setItem(AUTH_TOKEN, token)
  }
}

export default Login
