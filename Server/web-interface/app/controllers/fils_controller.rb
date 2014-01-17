class FilsController < ApplicationController
  # GET /fils
  # GET /fils.json
  def index
    @fils = Fil.all.paginate(:page => params[:page], :per_page => 30)

    respond_to do |format|
      format.html # index.html.erb
      format.json { render json: @fils }
    end
  end

  # GET /fils/1
  # GET /fils/1.json
  def show
    @fil = Fil.find(params[:id])

    respond_to do |format|
      format.html # show.html.erb
      format.json { render json: @fil }
    end
  end

  # GET /fils/new
  # GET /fils/new.json
  def new
    @fil = Fil.new

    respond_to do |format|
      format.html # new.html.erb
      format.json { render json: @fil }
    end
  end

  # GET /fils/1/edit
  def edit
    @fil = Fil.find(params[:id])
  end

  # POST /fils
  # POST /fils.json
  def create
    @fil = Fil.new(params[:fil])

    respond_to do |format|
      if @fil.save
        format.html { redirect_to @fil, notice: 'Fil was successfully created.' }
        format.json { render json: @fil, status: :created, location: @fil }
      else
        format.html { render action: "new" }
        format.json { render json: @fil.errors, status: :unprocessable_entity }
      end
    end
  end

  # PUT /fils/1
  # PUT /fils/1.json
  def update
    @fil = Fil.find(params[:id])

    respond_to do |format|
      if @fil.update_attributes(params[:fil])
        format.html { redirect_to @fil, notice: 'Fil was successfully updated.' }
        format.json { head :no_content }
      else
        format.html { render action: "edit" }
        format.json { render json: @fil.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /fils/1
  # DELETE /fils/1.json
  def destroy
    @fil = Fil.find(params[:id])
    @fil.destroy

    respond_to do |format|
      format.html { redirect_to fils_url }
      format.json { head :no_content }
    end
  end
end
