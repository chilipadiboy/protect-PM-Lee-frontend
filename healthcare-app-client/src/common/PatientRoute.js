import React from 'react';
import {
    Route,
    Redirect
  } from "react-router-dom";


const PatientRoute = ({ component: Component, authenticated, role, path }) => (
    <Route
      path
      render={props =>
        (authenticated && role === "patient") ? (
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

export default PatientRoute;
