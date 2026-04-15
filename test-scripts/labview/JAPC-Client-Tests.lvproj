<?xml version='1.0' encoding='UTF-8'?>
<Project Type="Project" LVVersion="17008000">
	<Property Name="CCSymbols" Type="Str"></Property>
	<Property Name="NI.LV.All.SaveVersion" Type="Str">17.0</Property>
	<Property Name="NI.LV.All.SourceOnly" Type="Bool">true</Property>
	<Property Name="NI.Project.Description" Type="Str"></Property>
	<Item Name="My Computer" Type="My Computer">
		<Property Name="server.app.propertiesEnabled" Type="Bool">true</Property>
		<Property Name="server.control.propertiesEnabled" Type="Bool">true</Property>
		<Property Name="server.tcp.enabled" Type="Bool">false</Property>
		<Property Name="server.tcp.port" Type="Int">0</Property>
		<Property Name="server.tcp.serviceName" Type="Str">My Computer/VI Server</Property>
		<Property Name="server.tcp.serviceName.default" Type="Str">My Computer/VI Server</Property>
		<Property Name="server.vi.callsEnabled" Type="Bool">true</Property>
		<Property Name="server.vi.propertiesEnabled" Type="Bool">true</Property>
		<Property Name="specify.custom.address" Type="Bool">false</Property>
		<Item Name="CorrectnessTests" Type="Folder" URL="../CorrectnessTests">
			<Property Name="NI.DISK" Type="Bool">true</Property>
		</Item>
		<Item Name="RTTTests" Type="Folder" URL="../RTTTests">
			<Property Name="NI.DISK" Type="Bool">true</Property>
		</Item>
		<Item Name="SLOCTests" Type="Folder" URL="../SLOCTests">
			<Property Name="NI.DISK" Type="Bool">true</Property>
		</Item>
		<Item Name="rade-japc-test01.vi" Type="VI" URL="../rade-japc-test01.vi"/>
		<Item Name="Dependencies" Type="Dependencies">
			<Item Name="user.lib" Type="Folder">
				<Item Name="AnalyzeError.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/AnalyzeError.vi"/>
				<Item Name="AppTimeout.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/AppTimeout.vi"/>
				<Item Name="Array of VData to VArray__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Array of VData to VArray__ogtk.vi"/>
				<Item Name="Array of VData to VCluster__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Array of VData to VCluster__ogtk.vi"/>
				<Item Name="Array Size(s)__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Array Size(s)__ogtk.vi"/>
				<Item Name="Array to Array of VData__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Array to Array of VData__ogtk.vi"/>
				<Item Name="Array to VCluster__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Array to VCluster__ogtk.vi"/>
				<Item Name="Build Error Cluster__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/error/error.llb/Build Error Cluster__ogtk.vi"/>
				<Item Name="CMWServer.lvclass" Type="LVClass" URL="/&lt;userlib&gt;/_RADE/cmw/server/CMWServer.lvclass"/>
				<Item Name="createNewBinaryToken.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/japc/vis/_etc/RBAC/createNewBinaryToken.vi"/>
				<Item Name="createNewToken.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/japc/vis/_etc/RBAC/createNewToken.vi"/>
				<Item Name="CreateTokenByLocation.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/CreateTokenByLocation.vi"/>
				<Item Name="DestroyToken.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/DestroyToken.vi"/>
				<Item Name="Get Array Element TDEnum__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Get Array Element TDEnum__ogtk.vi"/>
				<Item Name="Get Data Name from TD__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Get Data Name from TD__ogtk.vi"/>
				<Item Name="Get Data Name__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Get Data Name__ogtk.vi"/>
				<Item Name="Get Default Data from TD__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Get Default Data from TD__ogtk.vi"/>
				<Item Name="Get Element TD from Array TD__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Get Element TD from Array TD__ogtk.vi"/>
				<Item Name="Get Header from TD__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Get Header from TD__ogtk.vi"/>
				<Item Name="Get Last PString__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Get Last PString__ogtk.vi"/>
				<Item Name="Get PString__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Get PString__ogtk.vi"/>
				<Item Name="Get TDEnum from Data__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Get TDEnum from Data__ogtk.vi"/>
				<Item Name="Get Variant Attributes__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Get Variant Attributes__ogtk.vi"/>
				<Item Name="Get Waveform Type Enum from TD__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Get Waveform Type Enum from TD__ogtk.vi"/>
				<Item Name="GetAllTokenFields.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/GetAllTokenFields.vi"/>
				<Item Name="GetAppName.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/GetAppName.vi"/>
				<Item Name="GetArraySizes.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/GetArraySizes.vi"/>
				<Item Name="GetAuthTime.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/GetAuthTime.vi"/>
				<Item Name="GetBinaryToken.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/GetBinaryToken.vi"/>
				<Item Name="GetExpTime.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/GetExpTime.vi"/>
				<Item Name="GetLocationAdress.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/GetLocationAdress.vi"/>
				<Item Name="GetLocationAuthenticationRequest.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/GetLocationAuthenticationRequest.vi"/>
				<Item Name="GetLocName.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/GetLocName.vi"/>
				<Item Name="GetRoles.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/GetRoles.vi"/>
				<Item Name="GetSerialID.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/GetSerialID.vi"/>
				<Item Name="GetTokenIDs.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/GetTokenIDs.vi"/>
				<Item Name="GetUserName.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/GetUserName.vi"/>
				<Item Name="IsApplicationCritical.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/cmw/rbac/RBAC2LV/IsApplicationCritical.vi"/>
				<Item Name="japc_copies.lvlib" Type="Library" URL="/&lt;userlib&gt;/_RADE/japc/common_vis_ctl_from_other/japc_copies.lvlib"/>
				<Item Name="japc_dependencies.lvlib" Type="Library" URL="/&lt;userlib&gt;/_RADE/japc/vis/_etc/japc_dependencies.lvlib"/>
				<Item Name="JBase.lvclass" Type="LVClass" URL="/&lt;userlib&gt;/_RADE/japc/classes/JBase/JBase.lvclass"/>
				<Item Name="JGet.lvclass" Type="LVClass" URL="/&lt;userlib&gt;/_RADE/japc/classes/JGet/JGet.lvclass"/>
				<Item Name="JSet.lvclass" Type="LVClass" URL="/&lt;userlib&gt;/_RADE/japc/classes/JSet/JSet.lvclass"/>
				<Item Name="libCMWSW.dll" Type="Document" URL="/&lt;userlib&gt;/_RADE/libs/libCMWSW.dll"/>
				<Item Name="libRBAC.dll" Type="Document" URL="/&lt;userlib&gt;/_RADE/libs/libRBAC.dll"/>
				<Item Name="removeThisToken.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/japc/vis/_etc/RBAC/removeThisToken.vi"/>
				<Item Name="removeTokenByLocation.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/japc/vis/_etc/RBAC/removeTokenByLocation.vi"/>
				<Item Name="Reshape Array to 1D VArray__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Reshape Array to 1D VArray__ogtk.vi"/>
				<Item Name="Set Data Name__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Set Data Name__ogtk.vi"/>
				<Item Name="Split Cluster TD__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Split Cluster TD__ogtk.vi"/>
				<Item Name="String to 1D Array__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/string/string.llb/String to 1D Array__ogtk.vi"/>
				<Item Name="Tools_String Base64 Decode.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/mta-lib/string/StringTools_ogp/Tools_String Base64 Decode.vi"/>
				<Item Name="Tools_String Base64 Encode.vi" Type="VI" URL="/&lt;userlib&gt;/_RADE/mta-lib/string/StringTools_ogp/Tools_String Base64 Encode.vi"/>
				<Item Name="Type Descriptor Enumeration__ogtk.ctl" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Type Descriptor Enumeration__ogtk.ctl"/>
				<Item Name="Type Descriptor Header__ogtk.ctl" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Type Descriptor Header__ogtk.ctl"/>
				<Item Name="Type Descriptor__ogtk.ctl" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Type Descriptor__ogtk.ctl"/>
				<Item Name="Variant to Header Info__ogtk.vi" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Variant to Header Info__ogtk.vi"/>
				<Item Name="Waveform Subtype Enum__ogtk.ctl" Type="VI" URL="/&lt;userlib&gt;/_OpenG.lib/lvdata/lvdata.llb/Waveform Subtype Enum__ogtk.ctl"/>
			</Item>
			<Item Name="vi.lib" Type="Folder">
				<Item Name="Check if File or Folder Exists.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/libraryn.llb/Check if File or Folder Exists.vi"/>
				<Item Name="Clear Errors.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Clear Errors.vi"/>
				<Item Name="Error Cluster From Error Code.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Error Cluster From Error Code.vi"/>
				<Item Name="Get LV Class Path.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/LVClass/Get LV Class Path.vi"/>
				<Item Name="LabVIEWHTTPClient.lvlib" Type="Library" URL="/&lt;vilib&gt;/httpClient/LabVIEWHTTPClient.lvlib"/>
				<Item Name="NI_AALBase.lvlib" Type="Library" URL="/&lt;vilib&gt;/Analysis/NI_AALBase.lvlib"/>
				<Item Name="NI_FileType.lvlib" Type="Library" URL="/&lt;vilib&gt;/Utility/lvfile.llb/NI_FileType.lvlib"/>
				<Item Name="NI_PackedLibraryUtility.lvlib" Type="Library" URL="/&lt;vilib&gt;/Utility/LVLibp/NI_PackedLibraryUtility.lvlib"/>
				<Item Name="Path To Command Line String.vi" Type="VI" URL="/&lt;vilib&gt;/AdvancedString/Path To Command Line String.vi"/>
				<Item Name="PathToUNIXPathString.vi" Type="VI" URL="/&lt;vilib&gt;/Platform/CFURL.llb/PathToUNIXPathString.vi"/>
				<Item Name="Stall Data Flow.vim" Type="VI" URL="/&lt;vilib&gt;/Utility/Stall Data Flow.vim"/>
				<Item Name="subTimeDelay.vi" Type="VI" URL="/&lt;vilib&gt;/express/express execution control/TimeDelayBlock.llb/subTimeDelay.vi"/>
				<Item Name="Trim Whitespace One-Sided.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Trim Whitespace One-Sided.vi"/>
				<Item Name="Trim Whitespace.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Trim Whitespace.vi"/>
				<Item Name="whitespace.ctl" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/whitespace.ctl"/>
			</Item>
		</Item>
		<Item Name="Build Specifications" Type="Build"/>
	</Item>
</Project>
