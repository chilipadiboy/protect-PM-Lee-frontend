import React from 'react';
import {
    Route,
    Redirect
  } from "react-router-dom";
import NotFound from '../common/NotFound';


const PatientRoute = ({ component: Component, authenticated, role, path }) => (
    <Route
      path
      render={props =>
        (authenticated && role === "patient") ? (
          <Component path {...props} />
        ) : (
          <NotFound />
        )
      }
    />
);

export default PatientRoute;