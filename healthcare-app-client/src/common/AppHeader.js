import React, { Component } from 'react';
import {
    Link,
    withRouter
} from 'react-router-dom';
import './AppHeader.css';
import dataIcon from '../data.svg';
import { Layout, Menu, Dropdown, Icon } from 'antd';
import { AUTH_TOKEN } from '../constants';

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
        const authToken = localStorage.getItem(AUTH_TOKEN)
        let menuItems;
        if(authToken) {
          menuItems = [
            <Menu.Item key="/">
              <Link to="/">
                <Icon type="home" className="nav-icon" />
              </Link>
            </Menu.Item>,
            <Menu.Item key="/data">
            <Link to="/data">
              <img src={dataIcon} alt="data" className="data-icon" />
            </Link>
          </Menu.Item>,
            <Menu.Item key="/profile" className="profile-menu">
              <ProfileDropdownMenu
                handleMenuClick={this.handleMenuClick}/>
            </Menu.Item>
          ];
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
  const dropdownMenu = (
    <Menu onClick={props.handleMenuClick} className="profile-dropdown-menu">
      <Menu.Item key="logout" className="dropdown-item">
        Logout
      </Menu.Item>
    </Menu>
  );

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
