import React, { Component } from 'react';
import { getUserRecords } from '../../util/APIUtils';
import { Avatar, Tabs } from 'antd';
import { getAvatarColor } from '../../util/Colors';
import LoadingIndicator  from '../../common/LoadingIndicator';
import './Records.css';
import NotFound from '../../common/NotFound';
import ServerError from '../../common/ServerError';

class Records extends Component {
    constructor(props) {
        super(props);
        this.state = {
            user: null,
            isLoading: false
        }
        this.loadUserRecords = this.loadUserRecords.bind(this);
    }

    loadUserRecords(nric) {
        this.setState({
            isLoading: true
        });

        getUserRecords(nric)
        .then(response => {
            this.setState({
                user: response,
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

    componentDidMount() {
        const nric = this.props.match.params.nric;
        this.loadUserRecords(nric);
    }

    componentWillReceiveProps(nextProps) {
        if(this.props.match.params.nric !== nextProps.match.params.nric) {
            this.loadUserRecords(nextProps.match.params.nric);
        }
    }

    render() {
        if(this.state.isLoading) {
            return <LoadingIndicator />;
        }

        if(this.state.notFound) {
            return <NotFound />;
        }

        if(this.state.serverError) {
            return <ServerError />;
        }

        const tabBarStyle = {
            textAlign: 'center'
        };

        return (
            <div className="records">
                {
                    this.state.user ? (
                        <div className="user-records">
                            <div className="user-details">
                                <div className="user-avatar">
                                    <Avatar className="user-avatar-circle" style={{ backgroundColor: getAvatarColor(this.state.user.name)}}>
                                        {this.state.user.name[0].toUpperCase()}
                                    </Avatar>
                                </div>
                                <div className="user-summary">
                                    <div className="full-name">{this.state.user.name}</div>
                                    <div className="nric">@{this.state.user.nric}</div>
                                </div>
                            </div>
                            <div className="user-healthcare-details">
                                <Tabs defaultActiveKey="1"
                                    animated={false}
                                    tabBarStyle={tabBarStyle}
                                    size="large"
                                    className="records-tabs">
                                </Tabs>
                            </div>
                        </div>
                    ): null
                }
            </div>
        );
    }
}

export default Records;
