import React, { Component } from 'react'
import { AUTH_TOKEN } from '../constants'
import { Mutation } from 'react-apollo'
import gql from 'graphql-tag'

const SIGNUP_MUTATION = gql`
  mutation($nric: String!, $name: String!, $email: String!,
    $phone: String!, $address: String!, $age: Int!, $gender: String!, $password: String!) {
    register(nric: $nric, name: $name, email: $email, phone: $phone, address: $address, age: $age, gender: $gender, password: $password, name: $name)
  }
`

class Signup extends Component {
  state = {
    nric: '',
    name: '',
    email: '',
    phone: '',
    address: '',
    age: '',
    gender: '',
    password: '',
  }

  render() {
    const { login, nric, name, email, phone, address, age, gender, password } = this.state
    return (
      <div>
        <h4 className="mv3">{'Sign Up'}</h4>
        <div className="flex flex-column">
            <input
              value={nric}
              onChange={e => this.setState({ nric: e.target.value })}
              type="text"
              placeholder="Your nric"
            />
            <input
              value={name}
              onChange={e => this.setState({ name: e.target.value })}
              type="text"
              placeholder="Your name"
            />
            <input
              value={email}
              onChange={e => this.setState({ email: e.target.value })}
              type="text"
              placeholder="Your email address"
            />
            <input
              value={phone}
              onChange={e => this.setState({ phone: e.target.value })}
              type="text"
              placeholder="Your phone"
            />
            <input
              value={address}
              onChange={e => this.setState({ address: e.target.value })}
              type="text"
              placeholder="Your address"
            />
            <input
              value={age}
              onChange={e => this.setState({ age: e.target.value })}
              type="text"
              placeholder="Your age"
            />
            <input
              value={gender}
              onChange={e => this.setState({ gender: e.target.value })}
              type="text"
              placeholder="Your gender"
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
          mutation={SIGNUP_MUTATION}
          variables={{ nric, name, email, phone, address, age, gender, password }}
          onCompleted={data => this._confirm(data)}
        >
          {mutation => (
            <div className="pointer mr2 button" onClick={mutation}>
              create account
            </div>
          )}
        </Mutation>
          <div
            className="pointer button"
            onClick={() => this.setState({ login: !login })}
          >
            already have an account?
          </div>
        </div>
      </div>
    )
  }

  _confirm = async data => {
    this.props.history.push(`/`)
  }

  _saveUserData = token => {
    localStorage.setItem(AUTH_TOKEN, token)
  }
}

export default Signup
