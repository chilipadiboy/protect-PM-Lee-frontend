import React from 'react';
import {
    Route,
    Redirect
  } from "react-router-dom";
import NotFound from '../common/NotFound';


const TherapistRoute = ({ component: Component, authenticated, role, path }) => (
    <Route
      path
      render={props =>
        (authenticated && role === "therapist") ? (
          <Component path {...props} />
        ) : (
          <NotFound />
        )
      }
    />
);

export default TherapistRoute;