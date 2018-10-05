import React, { Component } from 'react';
import {
  Link,
  withRouter
} from 'react-router-dom';
import './AppHeader.css';
import dataIcon from '../data.svg';
import Therapist_mypatients from '../user/therapist/Mypatients'
import { Layout, Menu, Dropdown, Icon } from 'antd';

const Header = Layout.Header;

class AppHeader extends Component {
  constructor(props) {
    super(props);
    this.handleMenuClick = this.handleMenuClick.bind(this);
  }

  handleMenuClick({ key }) {
    if(key === "logout") {
      this.props.onLogout();
    }
  }

    render() {
        let menuItems;
        if(this.props.currentUser) {
          if(this.props.currentUser.role === "RESEARCHER") {
            menuItems = [
              <Menu.Item key="/generatedata">
                <Link to="/generatedata">
                  <Icon type="bar-chart" />
                </Link>
              </Menu.Item>,
              <Menu.Item key="/">
                <Link to="/">
                  <Icon type="home" className="nav-icon" />
                </Link>
              </Menu.Item>,
              <Menu.Item key="/profile" className="profile-menu">
                <ProfileDropdownMenu
                  currentUser={this.props.currentUser}
                  handleMenuClick={this.handleMenuClick}/>
              </Menu.Item>
            ];
          } else if(this.props.currentUser.role === "EXTERNAL_PARTNER") {
            menuItems = [
              <Menu.Item key="/uploaddatabase">
                <Link to="/uploaddatabase">
                  <Icon type="upload" />
                </Link>
              </Menu.Item>,
              <Menu.Item key="/">
                <Link to="/">
                  <Icon type="home" className="nav-icon" />
                </Link>
              </Menu.Item>,
              <Menu.Item key="/profile" className="profile-menu">
                <ProfileDropdownMenu
                  currentUser={this.props.currentUser}
                  handleMenuClick={this.handleMenuClick}/>
              </Menu.Item>
            ];
          } else if(this.props.currentUser.role === "PATIENT") {
            menuItems = [
              <Menu.Item key="/">
              <Link to="/">
              <Icon type="home" className="nav-icon" />
              </Link>
              </Menu.Item>,
              <Menu.Item key="/profile" className="profile-menu">
              <ProfileDropdownMenu
              currentUser={this.props.currentUser}
              handleMenuClick={this.handleMenuClick}/>
              </Menu.Item>
            ];            
            /*menuItems = [
              <Menu.Item key="/">
                <Link to="/">
                  <Icon type="home" className="nav-icon" />
                </Link>
              </Menu.Item>,
              <Menu.Item key="/profile" className="profile-menu">
                <ProfileDropdownMenu
                  currentUser={this.props.currentUser}
                  handleMenuClick={this.handleMenuClick}/>
              </Menu.Item>
            ];*/
          } else if(this.props.currentUser.role === "THERAPIST") {
            menuItems = [
              <Menu.Item key="/">
              <Link to="/">
              <Icon type="home" className="nav-icon" />
              </Link>
              </Menu.Item>,
              <Menu.Item key="/profile" className="profile-menu">
              <ProfileDropdownMenu
              currentUser={this.props.currentUser}
              handleMenuClick={this.handleMenuClick}/>
              </Menu.Item>
            ];
            /*menuItems = [
              <Menu.Item key="/therapist/mypatients">
                <Link to="/mypatients">
                  <Icon type="medicine-box" />
                </Link>
              </Menu.Item>,
              <Menu.Item key="/">
                <Link to="/">
                  <Icon type="home" className="nav-icon" />
                </Link>
              </Menu.Item>,
              <Menu.Item key="/profile" className="profile-menu">
                <ProfileDropdownMenu
                  currentUser={this.props.currentUser}
                  handleMenuClick={this.handleMenuClick}/>
              </Menu.Item>
            ];*/
          } else if(this.props.currentUser.role === "ADMINISTRATOR") {
            menuItems = [
              <Menu.Item key="/logs">
                <Link to="/logs">
                  <Icon type="database" />
                </Link>
              </Menu.Item>,
              <Menu.Item key="/manageusers">
                <Link to="/manageusers">
                  <Icon type="usergroup-add" />
                </Link>
              </Menu.Item>,
              <Menu.Item key="/">
                <Link to="/">
                  <Icon type="home" className="nav-icon" />
                </Link>
              </Menu.Item>,
              <Menu.Item key="/profile" className="profile-menu">
                <ProfileDropdownMenu
                  currentUser={this.props.currentUser}
                  handleMenuClick={this.handleMenuClick}/>
              </Menu.Item>
            ];
          } else {
            menuItems = [
              <Menu.Item key="/">
                <Link to="/">
                  <Icon type="home" className="nav-icon" />
                </Link>
              </Menu.Item>,
              <Menu.Item key="/profile" className="profile-menu">
                <ProfileDropdownMenu
                  currentUser={this.props.currentUser}
                  handleMenuClick={this.handleMenuClick}/>
              </Menu.Item>
            ];
          }
        } else {
          menuItems = [
            <Menu.Item key="/login">
              <Link to="/login">Login</Link>
            </Menu.Item>,
            <Menu.Item key="/signup">
              <Link to="/signup">Signup</Link>
            </Menu.Item>
          ];
        }

        return (
            <Header className="app-header">
            <div className="container">
              <div className="app-title" >
                <Link to="/">Healthcare App</Link>
              </div>
              <Menu
                className="app-menu"
                mode="horizontal"
                selectedKeys={[this.props.location.pathname]}
                style={{ lineHeight: '64px' }} >
                  {menuItems}
              </Menu>
            </div>
          </Header>
        );
      }
}

function ProfileDropdownMenu(props) {
  var dropdownMenu = '';
  if (props.currentUser.role === "PATIENT") {
    dropdownMenu = (
      <Menu onClick={props.handleMenuClick} className="profile-dropdown-menu">
      <Menu.Item key="user-info" className="dropdown-item" disabled>
      <div className="user-full-name-info">
      {props.currentUser.name}
      </div>
      <div className="nric-info">
      @{props.currentUser.nric}
      </div>
      </Menu.Item>
      <Menu.Divider />
      <Menu.Item key="records" className="dropdown-item">
      <Link to={`/records/${props.currentUser.role}/${props.currentUser.nric}`}>My Records</Link>
      </Menu.Item>
      <Menu.Item key="logout" className="dropdown-item">
      Logout
      </Menu.Item>
      </Menu>
    );
  } else if (props.currentUser.role === "THERAPIST") {
    dropdownMenu = (
      <Menu onClick={props.handleMenuClick} className="profile-dropdown-menu">
      <Menu.Item key="user-info" className="dropdown-item" disabled>
      <div className="user-full-name-info">
      {props.currentUser.name}
      </div>
      <div className="nric-info">
      @{props.currentUser.nric}
      </div>
      </Menu.Item>
      <Menu.Divider />
      <Menu.Item key="new record" className="dropdown-item">
      <Link to={`/create`}>New Record</Link>
      </Menu.Item>
      <Menu.Item key="records" className="dropdown-item">
      <Link to={`/all/`}>All Records</Link>
      </Menu.Item>
      <Menu.Item key="logout" className="dropdown-item">
      Logout
      </Menu.Item>
      </Menu>
    );
  }

  return (
    <Dropdown
    overlay={dropdownMenu}
    trigger={['click']}
    getPopupContainer = { () => document.getElementsByClassName('profile-menu')[0]}>
    <a className="ant-dropdown-link">
    <Icon type="user" className="nav-icon" style={{marginRight: 0}} /> <Icon type="down" />
    </a>
    </Dropdown>
  );
}

export default withRouter(AppHeader);
