import React from 'react';
import {
    Route,
    Redirect
  } from "react-router-dom";
import NotFound from '../common/NotFound';


const AdministratorRoute = ({ component: Component, authenticated, role, path }) => (
    <Route
      path
      render={props =>
        (authenticated && role === "administrator") ? (
          <Component path {...props} />
        ) : (
          <NotFound />
        )
      }
    />
);

export default AdministratorRoute;
