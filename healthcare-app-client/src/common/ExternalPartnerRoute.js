import React from 'react';
import {
    Route,
    Redirect
  } from "react-router-dom";


const ExternalPartnerRoute = ({ component: Component, authenticated, role, path }) => (
    <Route
      path
      render={props =>
        (authenticated && role === "external_partner") ? (
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

export default ExternalPartnerRoute;
