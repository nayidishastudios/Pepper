class ComitsController < ApplicationController
  # GET /repositories/:repository_id/comits
  # GET /repositories/:repository_id/comits.json
  def repository
    @comits = Comit.where(:repository_id => params[:repository_id]).paginate(:page => params[:page], :per_page => 30).order('time DESC')

    respond_to do |format|
      format.html { render file: 'comits/index.html.erb' } # index.html.erb
      format.json { render json: @comits }
    end
  end
  # GET /comits
  # GET /comits.json
  def index
    @comits = Comit.find(:all, :order => 'time DESC').paginate(:page => params[:page], :per_page => 30)

    respond_to do |format|
      format.html # index.html.erb
      format.json { render json: @comits }
    end
  end

  # GET /comits/1
  # GET /comits/1.json
  def show
    @comit = Comit.find(params[:id])

    respond_to do |format|
      format.html # show.html.erb
      format.json { render json: @comit }
    end
  end

  # GET /comits/new
  # GET /comits/new.json
  def new
    @comit = Comit.new

    respond_to do |format|
      format.html # new.html.erb
      format.json { render json: @comit }
    end
  end

  # GET /comits/1/edit
  def edit
    @comit = Comit.find(params[:id])
  end

  # POST /comits
  # POST /comits.json
  def create
    puts params
    @comit = Comit.new(params[:comit])

    respond_to do |format|
      if @comit.save
        format.html { redirect_to @comit, notice: 'commit was successfully created.' }
        format.json { render json: @comit, status: :created, location: @comit }
      else
        format.html { render action: "new" }
        format.json { render json: @comit.errors, status: :unprocessable_entity }
      end
    end
  end

  # PUT /comits/1
  # PUT /comits/1.json
  def update
    @comit = Comit.find(params[:id])

    respond_to do |format|
      if @comit.update_attributes(params[:comit])
        format.html { redirect_to @comit, notice: 'commit was successfully updated.' }
        format.json { head :no_content }
      else
        format.html { render action: "edit" }
        format.json { render json: @comit.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /comits/1
  # DELETE /comits/1.json
  def destroy
    @comit = Comit.find(params[:id])
    @comit.destroy

    respond_to do |format|
      format.html { redirect_to comits_url }
      format.json { head :no_content }
    end
  end
end
