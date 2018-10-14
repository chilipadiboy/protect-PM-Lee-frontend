import React from 'react';
import {
    Route,
    Redirect
  } from "react-router-dom";


const TherapistRoute = ({ component: Component, authenticated, role, path }) => (
    <Route
      path
      render={props =>
        (authenticated && role === "therapist") ? (
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

export default TherapistRoute;
