class InstallersController < ApplicationController
  # GET /installers
  # GET /installers.json
  def index
    @installers = Installer.all.paginate(:page => params[:page], :per_page => 30)

    respond_to do |format|
      format.html # index.html.erb
      format.json { render json: @installers }
    end
  end

  # GET /installers/1
  # GET /installers/1.json
  def show
    @installer = Installer.find(params[:id])

    respond_to do |format|
      format.html # show.html.erb
      format.json { render json: @installer }
    end
  end

  # GET /installers/new
  # GET /installers/new.json
  def new
    @installer = Installer.new

    respond_to do |format|
      format.html # new.html.erb
      format.json { render json: @installer }
    end
  end

  # GET /installers/1/edit
  def edit
    @installer = Installer.find(params[:id])
  end

  # POST /installers
  # POST /installers.json
  def create
    @installer = Installer.new(params[:installer])

    respond_to do |format|
      if @installer.save
        format.html { redirect_to @installer, notice: 'Installer was successfully created.' }
        format.json { render json: @installer, status: :created, location: @installer }
      else
        format.html { render action: "new" }
        format.json { render json: @installer.errors, status: :unprocessable_entity }
      end
    end
  end

  # PUT /installers/1
  # PUT /installers/1.json
  def update
    @installer = Installer.find(params[:id])

    respond_to do |format|
      if @installer.update_attributes(params[:installer])
        format.html { redirect_to @installer, notice: 'Installer was successfully updated.' }
        format.json { head :no_content }
      else
        format.html { render action: "edit" }
        format.json { render json: @installer.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /installers/1
  # DELETE /installers/1.json
  def destroy
    @installer = Installer.find(params[:id])
    @installer.destroy

    respond_to do |format|
      format.html { redirect_to installers_url }
      format.json { head :no_content }
    end
  end
end
