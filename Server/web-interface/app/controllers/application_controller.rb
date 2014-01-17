class ApplicationController < ActionController::Base
  require 'will_paginate/array'
  protect_from_forgery
  include SessionsHelper

  before_filter :check_auth

  # Force signout to prevent CSRF attacks
  def handle_unverified_request
    sign_out
    super
  end

  protected
  def check_auth
    if !signed_in?
      redirect_to '/signin', :flash => { :error => 'You need to be logged in to access that location.' }
    end
  end

  def forbid_signup_in_session
    if signed_in?
      flash[:error] = 'You need to be logged out to access that location.'
      redirect_to :controller => 'repositories'
    end
  end

  def forbid_login_in_session
    if signed_in?
      flash[:error] = 'You need to be logged out to access that location.'
      redirect_to :controller => 'repositories'
    end
  end
end
