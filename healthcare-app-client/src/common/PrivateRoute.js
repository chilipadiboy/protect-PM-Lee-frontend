import React from 'react';
import {
    Route,
    Redirect
  } from "react-router-dom";


const PrivateRoute = ({ component: Component, authenticated, path }) => (
    <Route
      path
      render={props =>
        authenticated ? (
          <Component path {...props} />
        ) : (
          <Redirect
            to={{
              pathname: '/',
              state: { from: props.location }
            }}
          />
        )
      }
    />
);

export default PrivateRoute;
