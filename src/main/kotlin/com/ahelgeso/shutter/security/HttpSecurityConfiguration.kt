package com.ahelgeso.shutter.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter


@Configuration
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

    override fun configure(httpSecurity: HttpSecurity) {
        // TODO: Don't allow access to actuator outside dev
        httpSecurity
                .authorizeRequests().antMatchers("/").permitAll()
    }

}