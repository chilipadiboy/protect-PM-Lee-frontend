import React from 'react';
import {
    Route,
    Redirect
  } from "react-router-dom";
import NotFound from '../common/NotFound';


const ExternalPartnerRoute = ({ component: Component, authenticated, role, path }) => (
    <Route
      path
      render={props =>
        (authenticated && role === "external_partner") ? (
          <Component path {...props} />
        ) : (
          <NotFound />
        )
      }
    />
);

export default ExternalPartnerRoute;
