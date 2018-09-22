import React, { Component } from 'react';
import './Signup.css';
import { Link } from 'react-router-dom';
import {
    NAME_MIN_LENGTH, NAME_MAX_LENGTH,
    NRIC_LENGTH,
    EMAIL_MAX_LENGTH,
    PHONE_LENGTH,
    MALE, FEMALE,
    PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH,
} from '../../constants';

import { Form, Input, Button } from 'antd';
import { Mutation } from 'react-apollo';
import gql from 'graphql-tag';

const FormItem = Form.Item;

const SIGNUP_MUTATION = gql`
  mutation($nric: String!, $name: String!, $email: String!, $phone: String!, $address: String!, $age: String!, $gender: String!, $password: String!) {
    register(nric: $nric, name: $name, email: $email, phone: $phone, address: $address, age: $age, gender: $gender, password: $password)
  }
`
var { nric, name, email, phone, address, age, gender, password } = ''

class Signup extends Component {
  constructor(props) {
        super(props);
        this.state = {
            nric: '',
          name: '',
          email: '',
          phone: '',
          address: '',
          age: '',
          gender: '',
          password: ''
        }
        this.handleInputChange = this.handleInputChange.bind(this);
        this.isFormInvalid = this.isFormInvalid.bind(this);
    }

    handleInputChange(event, validationFun) {
        const target = event.target;
        const inputName = target.name;
        const inputValue = target.value;

        this.setState({
            [inputName] : {
                value: inputValue,
                ...validationFun(inputValue)
            }
        });
    }

    isFormInvalid() {
        return !(this.state.nric.validateStatus === 'success' &&
            this.state.name.validateStatus === 'success' &&
            this.state.email.validateStatus === 'success' &&
            this.state.phone.validateStatus === 'success' &&
            this.state.address.validateStatus === 'success' &&
            this.state.age.validateStatus === 'success' &&
            this.state.gender.validateStatus === 'success' &&
            this.state.password.validateStatus === 'success'
        );
    }

    render() {
        return (
            <div className="signup-container">
                <h1 className="page-title">Sign Up</h1>
                <div className="signup-content">
                    <Form onSubmit={this.handleSubmit} className="signup-form">
                        <FormItem
                          label="NRIC"
                          hasFeedback
                          validateStatus={this.state.nric.validateStatus}
                          help={this.state.nric.errorMsg}>
                          <Input
                              size="large"
                              name="nric"
                              autoComplete="off"
                              onChange={(event) => {nric = event.target.value; this.handleInputChange(event, this.validateNric)}}  />
                        </FormItem>
                        <FormItem
                            label="Full Name"
                            hasFeedback
                            validateStatus={this.state.name.validateStatus}
                            help={this.state.name.errorMsg}>
                            <Input
                                size="large"
                                name="name"
                                autoComplete="off"
                                onChange={(event) => {name = event.target.value; this.handleInputChange(event, this.validateName)}}  />
                        </FormItem>
                        <FormItem
                            label="Email"
                            hasFeedback
                            validateStatus={this.state.email.validateStatus}
                            help={this.state.email.errorMsg}>
                            <Input
                                size="large"
                                name="email"
                                type="email"
                                autoComplete="off"
                                onBlur={this.validateEmailAvailability}
                                onChange={(event) => {email = event.target.value; this.handleInputChange(event, this.validateEmail)}} />
                        </FormItem>
                        <FormItem
                            label="Phone"
                            hasFeedback
                            validateStatus={this.state.phone.validateStatus}
                            help={this.state.phone.errorMsg}>
                            <Input
                                size="large"
                                name="phone"
                                autoComplete="off"
                                onChange={(event) => {phone = event.target.value; this.handleInputChange(event, this.validatePhone)}} />
                        </FormItem>
                        <FormItem
                            label="Address"
                            hasFeedback
                            validateStatus={this.state.address.validateStatus}
                            help={this.state.address.errorMsg}>
                            <Input
                                size="large"
                                name="address"
                                autoComplete="off"
                                onChange={(event) => {address = event.target.value; this.handleInputChange(event, this.validateAddress)}} />
                        </FormItem>
                        <FormItem
                            label="Age"
                            hasFeedback
                            validateStatus={this.state.age.validateStatus}
                            help={this.state.age.errorMsg}>
                            <Input
                                size="large"
                                name="age"
                                autoComplete="off"
                                onChange={(event) => {age = event.target.value; this.handleInputChange(event, this.validateAge)}} />
                        </FormItem>
                        <FormItem
                            label="Gender"
                            hasFeedback
                            validateStatus={this.state.gender.validateStatus}
                            help={this.state.gender.errorMsg}>
                            <Input
                                size="large"
                                name="gender"
                                autoComplete="off"
                                onChange={(event) => {gender = event.target.value; this.handleInputChange(event, this.validateGender)}} />
                        </FormItem>
                        <FormItem
                            label="Password"
                            hasFeedback
                            validateStatus={this.state.password.validateStatus}
                            help={this.state.password.errorMsg}>
                            <Input
                                size="large"
                                name="password"
                                type="password"
                                autoComplete="off"
                                placeholder="Between 6 to 20 characters"
                                onChange={(event) => {password = event.target.value; this.handleInputChange(event, this.validatePassword)}} />
                        </FormItem>
                        <FormItem>
                        <Mutation
                          mutation={SIGNUP_MUTATION}
                          variables={{ nric, name, email, phone, address, age, gender, password }}
                          onCompleted={data => this._confirm(data)}
                        >
                          {mutation => (
                            <Button type="primary"
                                onClick={mutation}
                                size="large"
                                className="signup-form-button"
                                >Sign up</Button>
                            )}
                            </Mutation>
                            Already registered? <Link to="/login">Login now!</Link>
                        </FormItem>
                    </Form>
                </div>
            </div>
        );
    }

    _confirm = async data => {
      this.props.history.push("/login");
    }

    // Validation Functions

    validateNric = (nric) => {
        if(nric.length < NRIC_LENGTH) {
            return {
                validateStatus: 'error',
                errorMsg: `NRIC is too short (${NRIC_LENGTH} characters needed.)`
            }
        } else if (nric.length > NRIC_LENGTH) {
            return {
                validationStatus: 'error',
                errorMsg: `NRIC is too long (${NRIC_LENGTH} characters allowed.)`
            }
        } else {
            return {
                validateStatus: 'success',
                errorMsg: null
            }
        }
    }

    validateName = (name) => {
        if(!name) {
            return {
                validateStatus: 'error',
                errorMsg: 'Name may not be empty'
              }
        }

        if(name.length < NAME_MIN_LENGTH) {
            return {
                validateStatus: 'error',
                errorMsg: `Name is too short (Minimum ${NAME_MIN_LENGTH} characters needed.)`
            }
        } else if (name.length > NAME_MAX_LENGTH) {
            return {
                validationStatus: 'error',
                errorMsg: `Name is too long (Maximum ${NAME_MAX_LENGTH} characters allowed.)`
            }
        } else {
            return {
                validateStatus: 'success',
                errorMsg: null,
              };
        }
    }

    validateEmail = (email) => {
        if(!email) {
            return {
                validateStatus: 'error',
                errorMsg: 'Email may not be empty'
            }
        }

        const EMAIL_REGEX = RegExp('[^@ ]+@[^@ ]+\\.[^@ ]+');
        if(!EMAIL_REGEX.test(email)) {
            return {
                validateStatus: 'error',
                errorMsg: 'Email not valid'
            }
        }

        if(email.length > EMAIL_MAX_LENGTH) {
            return {
                validateStatus: 'error',
                errorMsg: `Email is too long (Maximum ${EMAIL_MAX_LENGTH} characters allowed)`
            }
        }

        return {
            validateStatus: 'success',
            errorMsg: null
        }
    }

    validatePhone = (phone) => {
        if(!phone) {
            return {
                validateStatus: 'error',
                errorMsg: 'Phone may not be empty'
            }
        }

        const PHONE_REGEX = RegExp('^[0-9]*$');
        if(!PHONE_REGEX.test(phone)) {
            return {
                validateStatus: 'error',
                errorMsg: 'Phone not valid'
            }
        }

        if(phone.length < PHONE_LENGTH) {
            return {
                validateStatus: 'error',
                errorMsg: `Phone is too short (${PHONE_LENGTH} characters needed.)`
            }
        } else if (phone.length > PHONE_LENGTH) {
            return {
                validationStatus: 'error',
                errorMsg: `Phone is too long (${PHONE_LENGTH} characters allowed.)`
            }
        } else {
            return {
                validateStatus: 'success',
                errorMsg: null
            }
        }
    }

    validateAddress = (address) => {
        if(!address) {
            return {
                validateStatus: 'error',
                errorMsg: 'Address may not be empty'
              }
        } else {
            return {
                validateStatus: 'success',
                errorMsg: null,
              };
        }
    }

    validateAge = (age) => {
        if(!age) {
            return {
                validateStatus: 'error',
                errorMsg: 'Age may not be empty'
              }
        }

        const AGE_REGEX = RegExp('^[0-9]*$');
        if(!AGE_REGEX.test(age)) {
            return {
                validateStatus: 'error',
                errorMsg: 'Age not valid'
            }
        } else {
            return {
                validateStatus: 'success',
                errorMsg: null,
              };
        }
    }

    validateGender = (gender) => {
        if(!gender) {
            return {
                validateStatus: 'error',
                errorMsg: 'Gender may not be empty'
              }
        }

        if (gender === MALE || gender === FEMALE ||
        gender === MALE.toLowerCase() || gender === FEMALE.toLowerCase() ||
      gender === MALE.toUpperCase() || gender === FEMALE.toUpperCase()) {
          return {
            validateStatus: 'success',
            errorMsg: null,
            }
        } else {
            return {
              validateStatus: 'error',
              errorMsg: 'Must be male or female'
              };
        }
    }

    validatePassword = (password) => {
        if(password.length < PASSWORD_MIN_LENGTH) {
            return {
                validateStatus: 'error',
                errorMsg: `Password is too short (Minimum ${PASSWORD_MIN_LENGTH} characters needed.)`
            }
        } else if (password.length > PASSWORD_MAX_LENGTH) {
            return {
                validationStatus: 'error',
                errorMsg: `Password is too long (Maximum ${PASSWORD_MAX_LENGTH} characters allowed.)`
            }
        } else {
            return {
                validateStatus: 'success',
                errorMsg: null,
            };
        }
    }

}

export default Signup;
