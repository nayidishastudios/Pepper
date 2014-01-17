class SessionsController < ApplicationController

  skip_before_filter :check_auth
  before_filter :forbid_login_in_session, :only => [:create, :new]

  def new
  end

  def create
    user = User.find_by_email(params[:session][:email].downcase)
    if user && user.authenticate(params[:session][:password])
      sign_in user
      redirect_to :controller => 'repositories'
    else
      flash.now[:error] = 'Invalid email/password combination'
      render 'new'
    end
  end

  def destroy
    sign_out
    redirect_to '/signin', :notice => 'You were successfully logged out.'
  end

end
