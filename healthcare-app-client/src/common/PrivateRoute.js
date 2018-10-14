import React from 'react';
import {
    Route,
    Redirect
  } from "react-router-dom";
import NotFound from '../common/NotFound';


const PrivateRoute = ({ component: Component, authenticated, path }) => (
    <Route
      path
      render={props =>
        authenticated ? (
          <Component path {...props} />
        ) : (
          <NotFound />
        )
      }
    />
);

export default PrivateRoute;
